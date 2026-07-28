package com.alin.lin.service.impl;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.dao.UserRoleAuthorizationDao;
import com.alin.lin.dto.UserRoleAuthorizationDto;
import com.alin.lin.dto.UserRoleAuthorizationRequest;
import com.alin.lin.dto.UserAccountCreateRequest;
import com.alin.lin.dto.UserAccountUpdateRequest;
import com.alin.lin.dto.UserPasswordResetRequest;
import com.alin.lin.dto.UserScreenAuthorizationDto;
import com.alin.lin.dto.UserScreenAuthorizationRequest;
import com.alin.lin.entity.ChangeReview;
import com.alin.lin.entity.ChangeReviewAudit;
import com.alin.lin.service.UserRoleAuthorizationService;
import com.alin.lin.service.CodeTableCacheService;
import com.alin.lin.service.support.PendingReviewGuard;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.AntPathMatcher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.alin.lin.util.UuidV7;
import org.slf4j.MDC;

@Service
public class UserRoleAuthorizationServiceImpl implements UserRoleAuthorizationService {
    private static final String FUNCTION_CODE = "MUS00001";
    private static final Set<String> ALLOWED_ROLES = Set.of("MAKER", "REVIEWER", "USER", "ADMIN");
    private static final AntPathMatcher API_PATH_MATCHER = new AntPathMatcher();

    private final UserRoleAuthorizationDao userRoleAuthorizationDao;
    private final PolicyChangeDao policyChangeDao;
    private final CodeTableCacheService codeTableCacheService;
    private final ObjectMapper objectMapper;
    private final PendingReviewGuard pendingReviewGuard;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserRoleAuthorizationServiceImpl(UserRoleAuthorizationDao userRoleAuthorizationDao, PolicyChangeDao policyChangeDao,
                                            CodeTableCacheService codeTableCacheService, ObjectMapper objectMapper,
                                            PendingReviewGuard pendingReviewGuard, PasswordEncoder passwordEncoder) {
        this.userRoleAuthorizationDao = userRoleAuthorizationDao;
        this.policyChangeDao = policyChangeDao;
        this.codeTableCacheService = codeTableCacheService;
        this.objectMapper = objectMapper;
        this.pendingReviewGuard = pendingReviewGuard;
        this.passwordEncoder = passwordEncoder;
    }

    // Convenience ctor for tests and legacy callers
    public UserRoleAuthorizationServiceImpl(UserRoleAuthorizationDao userRoleAuthorizationDao, PolicyChangeDao policyChangeDao,
                                            ObjectMapper objectMapper, PendingReviewGuard pendingReviewGuard) {
        this(userRoleAuthorizationDao, policyChangeDao,
                new com.alin.lin.service.impl.CodeTableCacheServiceImpl(policyChangeDao),
                objectMapper, pendingReviewGuard,
                com.alin.lin.config.PasswordEncodingConfig.createPasswordEncoder());
    }

    @Override
    public List<UserRoleAuthorizationDto> findAll() {
        Map<String, UserRoleAuthorizationDto> users = new LinkedHashMap<>();
        userRoleAuthorizationDao.findAllUserAccounts().forEach(record -> {
            String userId = String.valueOf(record.get("user_id"));
            users.put(userId, UserRoleAuthorizationDto.builder()
                    .userId(userId)
                    .enabled(Boolean.TRUE.equals(record.get("enabled")))
                    .roles(new ArrayList<>())
                    .reviewStatus("S")
                    .createdBy(String.valueOf(record.get("created_by")))
                    .createdAt(toLocalDateTime(record.get("created_at")))
                    .updatedBy(String.valueOf(record.get("updated_by")))
                    .updatedAt(toLocalDateTime(record.get("updated_at")))
                    .build());
        });
        userRoleAuthorizationDao.findAllRoleAssignments().forEach(record -> {
            UserRoleAuthorizationDto user = users.get(String.valueOf(record.get("user_id")));
            if (user != null) user.getRoles().add(normalizeRole(String.valueOf(record.get("role_code"))));
        });
        userRoleAuthorizationDao.findLatestReviewStatuses(FUNCTION_CODE).forEach(record -> {
            UserRoleAuthorizationDto user = users.get(String.valueOf(record.get("unique_key")));
            if (user != null) user.setReviewStatus(String.valueOf(record.get("review_status")));
        });
        return new ArrayList<>(users.values());
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"userFunctionCodes", "userSecurityDetails"}, key = "#request.userId")
    public UserRoleAuthorizationDto createUser(UserAccountCreateRequest request, String operatorId) {
        String userId = request.getUserId().trim();
        if (userRoleAuthorizationDao.countByUserId(userId) > 0) throw new IllegalArgumentException("使用者 ID 已存在: " + userId);
        if (userId.equals(request.getPassword())) throw new IllegalArgumentException("初始密碼不可與使用者 ID 相同");
        List<String> roles = validateRoles(request.getRoles());
        userRoleAuthorizationDao.insertUserAccount(UuidV7.next(), userId, passwordEncoder.encode(request.getPassword()), request.isEnabled(), operatorId, operatorId);
        for (String role : roles) {
            userRoleAuthorizationDao.insertUserRoleAssignment(UuidV7.next(), userId, "ROLE_" + role, operatorId, operatorId);
        }
        writeCompletedAudit(userId, "CREATE", Map.of(),
                Map.of("userId", userId, "enabled", request.isEnabled(), "roles", roles), operatorId);
        return findAll().stream().filter(user -> user.getUserId().equals(userId)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("找不到使用者: " + userId));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"userFunctionCodes", "userSecurityDetails"}, key = "#request.userId")
    public UserRoleAuthorizationDto updateUser(UserAccountUpdateRequest request, String operatorId) {
        String userId = request.getUserId().trim();
        ensureNoPendingReview(userId);
        ensureUserExists(userId);
        List<String> beforeRoles = findRoles(userId);
        boolean beforeEnabled = userRoleAuthorizationDao.findEnabledByUserId(userId);
        List<String> roles = validateRoles(request.getRoles());
        if (userId.equals(operatorId) && (!request.isEnabled() || !roles.contains("ADMIN"))) {
            throw new IllegalArgumentException("不可停用自己的帳號或移除自己的授權管理角色");
        }
        if (beforeEnabled && beforeRoles.contains("ADMIN")
                && (!request.isEnabled() || !roles.contains("ADMIN")) && countOtherEnabledAdmins(userId) == 0) {
            throw new IllegalArgumentException("系統至少必須保留一個啟用中的授權管理員");
        }
        userRoleAuthorizationDao.deleteUserRoleAssignments(userId);
        for (String role : roles) {
            userRoleAuthorizationDao.insertUserRoleAssignment(UuidV7.next(), userId, "ROLE_" + role, operatorId, operatorId);
        }
        userRoleAuthorizationDao.updateUserAccountEnabled(userId, request.isEnabled(), operatorId);
        writeCompletedAudit(userId, "UPDATE",
                Map.of("userId", userId, "enabled", beforeEnabled, "roles", beforeRoles),
                Map.of("userId", userId, "enabled", request.isEnabled(), "roles", roles), operatorId);
        return findAll().stream().filter(user -> user.getUserId().equals(userId)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("找不到使用者: " + userId));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "userSecurityDetails", key = "#rawUserId")
    public void resetPassword(String rawUserId, UserPasswordResetRequest request, String operatorId) {
        String userId = rawUserId.trim();
        ensureUserExists(userId);
        if (userId.equals(request.getPassword())) throw new IllegalArgumentException("新密碼不可與使用者 ID 相同");
        int updated = userRoleAuthorizationDao.updateUserAccountPassword(userId, passwordEncoder.encode(request.getPassword()), operatorId);
        if (updated != 1) throw new IllegalStateException("重設密碼失敗");
        writeCompletedAudit(userId, "UPDATE",
                Map.of("userId", userId, "passwordReset", false),
                Map.of("userId", userId, "passwordReset", true), operatorId);
    }

    private int countOtherEnabledAdmins(String userId) {
        Integer count = userRoleAuthorizationDao.countOtherEnabledAdmins(userId);
        return count == null ? 0 : count;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"userFunctionCodes", "userSecurityDetails"}, key = "#request.userId")
    public UserRoleAuthorizationDto addRoles(UserRoleAuthorizationRequest request, String operatorId) {
        ensureNoPendingReview(request.getUserId());
        List<String> before = findRoles(request.getUserId());
        Set<String> merged = new LinkedHashSet<>(before);
        merged.addAll(validateRoles(request.getRoles()));
        return save(request.getUserId(), before, new ArrayList<>(merged), "CREATE", operatorId);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"userFunctionCodes", "userSecurityDetails"}, key = "#request.userId")
    public UserRoleAuthorizationDto replaceRoles(UserRoleAuthorizationRequest request, String operatorId) {
        ensureNoPendingReview(request.getUserId());
        List<String> before = findRoles(request.getUserId());
        return save(request.getUserId(), before, validateRoles(request.getRoles()), "UPDATE", operatorId);
    }

    @Override
    public List<UserScreenAuthorizationDto> findAllScreenAuthorizations() {
        Map<String, UserScreenAuthorizationDto> users = new LinkedHashMap<>();
        userRoleAuthorizationDao.findAllUserAccounts().forEach(record -> {
            String userId = String.valueOf(record.get("user_id"));
            users.put(userId, UserScreenAuthorizationDto.builder().userId(userId)
                    .functionCodes(new ArrayList<>()).reviewStatus("S").build());
        });
        userRoleAuthorizationDao.findAllScreenAuthorizationRows().forEach(record -> {
            UserScreenAuthorizationDto user = users.get(String.valueOf(record.get("user_id")));
            if (user == null) return;
            user.getFunctionCodes().add(String.valueOf(record.get("function_code")));
            LocalDateTime createdAt = toLocalDateTime(record.get("created_at"));
            LocalDateTime updatedAt = toLocalDateTime(record.get("updated_at"));
            if (user.getCreatedAt() == null || createdAt.isBefore(user.getCreatedAt())) {
                user.setCreatedBy(String.valueOf(record.get("created_by")));
                user.setCreatedAt(createdAt);
            }
            if (user.getUpdatedAt() == null || updatedAt.isAfter(user.getUpdatedAt())) {
                user.setUpdatedBy(String.valueOf(record.get("updated_by")));
                user.setUpdatedAt(updatedAt);
            }
        });
        return new ArrayList<>(users.values());
    }

    /**
     * MySQL Connector/J 依版本與 MyBatis 型別處理器可能回傳 LocalDateTime 或 Timestamp。
     * Service 在資料邊界統一轉換，避免授權查詢因驅動差異發生 ClassCastException。
     */
    private static LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime localDateTime) return localDateTime;
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toLocalDateTime();
        throw new IllegalArgumentException("不支援的日期時間型態: " + value.getClass().getName());
    }

    @Override
    // 使用者畫面授權以 userId 為快取鍵；角色不直接展開畫面，畫面來源仍是資料庫授權表。
    @Cacheable(cacheNames = "userFunctionCodes", key = "#userId")
    public List<String> findFunctionCodes(String userId) {
        return userRoleAuthorizationDao.findFunctionCodes(userId);
    }

    @Override
    // 所有可授權畫面來自 main-screen code table，供本機模式及授權管理畫面共用。
    @Cacheable(cacheNames = "availableFunctionCodes", key = "'all'")
    public List<String> findAvailableFunctionCodes() {
        // 關閉安全驗證的本機模式也以 code table 為唯一畫面清單來源。
        return userRoleAuthorizationDao.findAvailableFunctionCodes();
    }

    @Override
    // Filter 只傳入請求條件；API 與功能代碼配對由 DAO 讀取 code table 後在 Service 選出最具體規則。
    @Cacheable(cacheNames = "apiFunctionCodes", key = "#httpMethod + '|' + #requestPath")
    public List<String> findApiFunctionCodes(String httpMethod, String requestPath) {
        // 支援一般路徑前綴及 Ant 樣式動態路徑；以最具體規則優先，避免共用 API 蓋過明細 API。
        List<Map<String, Object>> rules = userRoleAuthorizationDao.findApiFunctionCodes(httpMethod);
        Optional<Map<String, Object>> selected = rules.stream()
                .filter(rule -> matchesApiPath(String.valueOf(rule.get("code_before")), requestPath))
                .sorted((left, right) -> {
                    int pathSpecificity = Integer.compare(
                            String.valueOf(right.get("code_before")).length(),
                            String.valueOf(left.get("code_before")).length());
                    if (pathSpecificity != 0) return pathSpecificity;
                    boolean leftExactMethod = httpMethod.equals(left.get("code_field"));
                    boolean rightExactMethod = httpMethod.equals(right.get("code_field"));
                    return Boolean.compare(rightExactMethod, leftExactMethod);
                })
                .findFirst();
        if (selected.isEmpty() || selected.get().get("code_after") == null) return List.of();
        return java.util.Arrays.stream(String.valueOf(selected.get().get("code_after")).split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .distinct()
                .toList();
    }

    private boolean matchesApiPath(String configuredPath, String requestPath) {
        if (configuredPath.contains("*") || configuredPath.contains("?")) {
            return API_PATH_MATCHER.match(configuredPath, requestPath);
        }
        return requestPath.startsWith(configuredPath);
    }

    @Override
    @Transactional
    // 畫面授權變更後清除所有衍生授權快取，避免同一使用者仍看到舊選單或沿用舊 API 權限。
    @CacheEvict(cacheNames = {"userFunctionCodes", "apiFunctionCodes", "availableFunctionCodes"}, allEntries = true)
    public UserScreenAuthorizationDto replaceScreens(UserScreenAuthorizationRequest request, String operatorId) {
        String userId = request.getUserId();
        String uniqueKey = "SCREEN|" + userId;
        pendingReviewGuard.requireNoPending(FUNCTION_CODE, uniqueKey, "使用者 ID " + userId + " 的畫面授權");
        ensureUserExists(userId);
        List<Map<String, Object>> beforeRows = findScreenAuthorizationRows(userId);
        List<String> after = request.getFunctionCodes().stream().map(String::trim).distinct().sorted().toList();
        // 可授權畫面以資料庫 main-screen/function_code 為準，不由角色或 Java 常數帶出。
        Set<String> allowedFunctionCodes = new LinkedHashSet<>(findAvailableFunctionCodes());
        if (!allowedFunctionCodes.containsAll(after)) throw new IllegalArgumentException("包含不支援的功能代碼");
        userRoleAuthorizationDao.deleteUserScreenAuthorizations(userId);
        for (String code : after) {
            userRoleAuthorizationDao.insertUserScreenAuthorization(UuidV7.next(), userId, code, operatorId, operatorId);
        }
        List<Map<String, Object>> afterRows = findScreenAuthorizationRows(userId);
        writeScreenCompletedAudit(uniqueKey, "UPDATE", beforeRows, afterRows, operatorId);
        return findAllScreenAuthorizations().stream().filter(user -> user.getUserId().equals(userId)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("找不到使用者: " + userId));
    }

    private UserRoleAuthorizationDto save(String userId, List<String> before, List<String> after,                                          String operation, String operatorId) {
        ensureUserExists(userId);
        userRoleAuthorizationDao.deleteUserRoleAssignments(userId);
        for (String role : after) {
            userRoleAuthorizationDao.insertUserRoleAssignment(UuidV7.next(), userId, "ROLE_" + role, operatorId, operatorId);
        }
        userRoleAuthorizationDao.updateUserAccountUpdatedBy(userId, operatorId);
        writeCompletedAudit(userId, operation, before, after, operatorId);
        return findAll().stream().filter(user -> user.getUserId().equals(userId)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("找不到使用者: " + userId));
    }

    private void ensureNoPendingReview(String userId) {
        // 相同功能代碼與 userId 是同一業務 Key；FOR UPDATE 避免檢查期間被另一交易同時處理。
        pendingReviewGuard.requireNoPending(FUNCTION_CODE, userId, "使用者 " + userId + " 的角色異動");
    }

    private List<String> findRoles(String userId) {
        ensureUserExists(userId);
        return userRoleAuthorizationDao.findRoleCodesByUserId(userId).stream()
                .map(UserRoleAuthorizationServiceImpl::normalizeRole).toList();
    }

    private List<Map<String, Object>> findScreenAuthorizationRows(String userId) {
        return userRoleAuthorizationDao.findScreenAuthorizationRows(userId);
    }

    private void ensureUserExists(String userId) {
        if (userRoleAuthorizationDao.countByUserId(userId) == 0) throw new NoSuchElementException("找不到使用者: " + userId);
    }

    private List<String> validateRoles(List<String> roles) {
        List<String> normalized = roles.stream().map(UserRoleAuthorizationServiceImpl::normalizeRole).distinct().sorted().toList();
        if (normalized.isEmpty()) throw new IllegalArgumentException("至少選擇一個角色");
        if (!ALLOWED_ROLES.containsAll(normalized)) throw new IllegalArgumentException("包含不支援的角色");
        return normalized;
    }

    private void writeCompletedAudit(String userId, String operation, List<String> before,
                                     List<String> after, String operatorId) {
        writeCompletedAudit(userId, operation, Map.of("userId", userId, "roles", before),
                Map.of("userId", userId, "roles", after), operatorId);
    }

    private void writeCompletedAudit(String userId, String operation, Map<String, Object> before,
                                     Map<String, Object> after, String operatorId) {
        try {
            String beforeJson = objectMapper.writeValueAsString(before);
            String afterJson = objectMapper.writeValueAsString(after);
            String reviewKey = FUNCTION_CODE + "|" + userId + "|" + UUID.randomUUID();
            ChangeReview review = ChangeReview.builder()
                    .reviewUuid(UuidV7.next())
                    .operation(operation)
                    .workflowMode("DIRECT")
                    .sourceType("USER_AUTHORIZATION")
                    .sourceRecordType("AUTHORITIES")
                    .functionCode(FUNCTION_CODE)
                    .key1(userId)
                    .uniqueKey(userId)
                    .reviewKey(reviewKey)
                    .contentBefore(beforeJson)
                    .contentAfter(afterJson)
                    .reviewStatus("S")
                    .createdBy(operatorId)
                    .reviewedBy(operatorId)
                    .build();
            if (policyChangeDao.insertCompletedChangeReview(review) != 1 || review.getId() == null) {
                throw new IllegalStateException("建立使用者授權稽核主檔失敗");
            }
            ChangeReviewAudit audit = ChangeReviewAudit.builder()
                    .eventId(UuidV7.next())
                    .reviewId(review.getId())
                    .reviewKey(reviewKey)
                    .functionCode(FUNCTION_CODE)
                    .action("DIRECT_APPLY")
                    .statusAfter("S")
                    .operatorId(operatorId)
                    .reviewRemark("Admin 直接完成使用者角色授權")
                    .contentBefore(beforeJson)
                    .contentAfter(afterJson)
                    .requestId(MDC.get("requestId"))
                    .traceId(MDC.get("traceId"))
                    .occurredAt(LocalDateTime.now())
                    .build();
            if (policyChangeDao.insertChangeReviewAudit(audit) != 1) {
                throw new IllegalStateException("建立使用者授權稽核歷程失敗");
            }
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("使用者授權稽核內容轉換失敗", exception);
        }
    }

    private void writeScreenCompletedAudit(String userId, String operation, List<Map<String, Object>> before,
                                           List<Map<String, Object>> after, String operatorId) {
        try {
            // 稽核快照保存 user_screen_authorization 每一列的全部欄位，不以 functionCodes 清單取代資料列。
            String beforeJson = objectMapper.writeValueAsString(Map.of("rows", before));
            String afterJson = objectMapper.writeValueAsString(Map.of("rows", after));
            String uniqueKey = "SCREEN|" + userId;
            String reviewKey = FUNCTION_CODE + "|" + uniqueKey + "|" + UUID.randomUUID();
            ChangeReview review = ChangeReview.builder().reviewUuid(UuidV7.next())
                    .operation(operation).sourceType("USER_AUTHORIZATION")
                    .sourceRecordType("USER_SCREEN")
                    .workflowMode("DIRECT").functionCode(FUNCTION_CODE).key1(userId)
                    .uniqueKey(uniqueKey).reviewKey(reviewKey)
                    .contentBefore(beforeJson).contentAfter(afterJson).reviewStatus("S")
                    .createdBy(operatorId).reviewedBy(operatorId).build();
            if (policyChangeDao.insertCompletedChangeReview(review) != 1 || review.getId() == null)
                throw new IllegalStateException("建立畫面授權稽核主檔失敗");
            ChangeReviewAudit audit = ChangeReviewAudit.builder().eventId(UuidV7.next())
                    .reviewId(review.getId()).reviewKey(reviewKey).functionCode(FUNCTION_CODE).action("DIRECT_APPLY")
                    .statusAfter("S").operatorId(operatorId).reviewRemark("Admin 直接完成畫面授權")
                    .contentBefore(beforeJson).contentAfter(afterJson)
                    .requestId(MDC.get("requestId")).traceId(MDC.get("traceId"))
                    .occurredAt(LocalDateTime.now()).build();
            if (policyChangeDao.insertChangeReviewAudit(audit) != 1)
                throw new IllegalStateException("建立畫面授權稽核歷程失敗");
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("畫面授權稽核內容轉換失敗", exception);
        }
    }

    private static String normalizeRole(String role) {
        return role == null ? "" : role.replaceFirst("^ROLE_", "").trim().toUpperCase();
    }
}
