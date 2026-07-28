package com.alin.lin.service.impl;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.dto.CodeDescriptionCreateRequest;
import com.alin.lin.entity.CodeDescription;
import com.alin.lin.entity.ChangeReview;
import com.alin.lin.enums.CodeDescriptionMeaning;
import com.alin.lin.enums.CodeTable;
import com.alin.lin.service.CodeDescriptionService;
import com.alin.lin.service.CodeTableCacheService;
import com.alin.lin.service.ChangeReviewService;
import com.alin.lin.service.support.PendingReviewGuard;
import com.alin.lin.service.ChangeReviewApplier;
import com.alin.lin.service.policy.ReviewExecutionPolicy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.NoSuchElementException;
import java.time.LocalDateTime;
import java.util.UUID;
import com.alin.lin.util.UuidV7;

@Service
public class CodeDescriptionServiceImpl implements CodeDescriptionService {
    /** 代碼新增、修改的稽核歸屬異動畫面；MCM00001 僅供查詢。 */
    private static final String CODE_TABLE_FUNCTION_CODE = "MCM00002";

    private final PolicyChangeDao policyChangeDao;
    private final CodeTableCacheService codeTableCacheService;
    private final ChangeReviewService changeReviewService;
    private final ObjectMapper objectMapper;
    private final PendingReviewGuard pendingReviewGuard;
    private final ChangeReviewApplier changeReviewApplier;
    private final ReviewExecutionPolicy reviewExecutionPolicy;

    public CodeDescriptionServiceImpl(
            PolicyChangeDao policyChangeDao,
            CodeTableCacheService codeTableCacheService,
            ChangeReviewService changeReviewService,
            ObjectMapper objectMapper,
            PendingReviewGuard pendingReviewGuard,
            ChangeReviewApplier changeReviewApplier,
            ReviewExecutionPolicy reviewExecutionPolicy
    ) {
        this.policyChangeDao = policyChangeDao;
        this.codeTableCacheService = codeTableCacheService;
        this.changeReviewService = changeReviewService;
        this.objectMapper = objectMapper;
        this.pendingReviewGuard = pendingReviewGuard;
        this.changeReviewApplier = changeReviewApplier;
        this.reviewExecutionPolicy = reviewExecutionPolicy;
    }

    @Override
    public List<CodeDescription> findAllCodes() {
        return policyChangeDao.findAllCodes();
    }

    @Override
    @Transactional
    // code_definition 同時承載欄位中文、畫面清單及 API 授權，異動後必須清除全部衍生快取。
    @CacheEvict(cacheNames = {"codeTableCodes", "codeTableCode", "codeTableCodesByGroup",
            "apiFunctionCodes", "availableFunctionCodes"}, allEntries = true)
    public CodeDescription createCode(CodeDescriptionCreateRequest request, String username) {
        String uniqueKey = request.getCodeGroup() + "|" + request.getCodeField() + "|" + request.getCodeBefore();
        pendingReviewGuard.requireNoPending(CODE_TABLE_FUNCTION_CODE, uniqueKey, "代碼對照表");
        if (codeTableCacheService.findCode(request.getCodeGroup(), request.getCodeField(), request.getCodeBefore()) != null) {
            throw new IllegalArgumentException("已有相同 Key：" + uniqueKey);
        }
        CodeDescription code = CodeDescription.builder()
                .codeGroup(request.getCodeGroup()).codeField(request.getCodeField())
                .codeBefore(request.getCodeBefore()).codeAfter(request.getCodeAfter())
                .codeDescription(request.getCodeDescription()).createdBy(username).createdAt(java.time.LocalDateTime.now())
                .activeFlag("Y").reviewStatus("P").build();
        saveReview("CREATE", code, null, code, username);
        return code;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"codeTableCodes", "codeTableCode", "codeTableCodesByGroup",
            "apiFunctionCodes", "availableFunctionCodes"}, allEntries = true)
    public CodeDescription updateCode(CodeDescriptionCreateRequest request, String username) {
        String originalGroup = request.getOriginalCodeGroup() == null ? request.getCodeGroup() : request.getOriginalCodeGroup();
        String originalField = request.getOriginalCodeField() == null ? request.getCodeField() : request.getOriginalCodeField();
        String originalBefore = request.getOriginalCodeBefore() == null ? request.getCodeBefore() : request.getOriginalCodeBefore();
        pendingReviewGuard.requireNoPending(CODE_TABLE_FUNCTION_CODE,
                originalGroup + "|" + originalField + "|" + originalBefore, "代碼對照表");
        CodeDescription existing = codeTableCacheService.findCode(originalGroup, originalField, originalBefore);
        if (existing == null) {
            throw new NoSuchElementException("找不到要修改的代碼: " + originalGroup + "." + originalField + "." + originalBefore);
        }
        if (!request.getCodeGroup().equals(originalGroup) || !request.getCodeField().equals(originalField)) {
            throw new IllegalArgumentException("代碼群組與欄位不可修改");
        }
        boolean keyChanged = !request.getCodeBefore().equals(originalBefore);
        CodeDescription target = policyChangeDao.findCode(request.getCodeGroup(), request.getCodeField(), request.getCodeBefore());
        if (keyChanged && target != null) {
            throw new IllegalArgumentException("已有相同 Key：" + request.getCodeGroup() + "." + request.getCodeField() + "." + request.getCodeBefore());
        }
        CodeDescription updated = CodeDescription.builder()
                .codeGroup(request.getCodeGroup()).codeField(request.getCodeField()).codeBefore(request.getCodeBefore())
                .codeAfter(request.getCodeAfter()).codeDescription(request.getCodeDescription())
                .activeFlag(existing.getActiveFlag())
                .reviewStatus("P").recordVersion(existing.getRecordVersion())
                .createdBy(existing.getCreatedBy()).createdAt(existing.getCreatedAt())
                .updatedBy(username).updatedAt(LocalDateTime.now()).build();
        saveReview("UPDATE", updated, existing, updated, username);
        return updated;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"codeTableCodes", "codeTableCode", "codeTableCodesByGroup",
            "apiFunctionCodes", "availableFunctionCodes"}, allEntries = true)
    public void deleteCode(String codeGroup, String codeField, String codeBefore, String username) {
        String uniqueKey = codeGroup + "|" + codeField + "|" + codeBefore;
        pendingReviewGuard.requireNoPending(CODE_TABLE_FUNCTION_CODE, uniqueKey, "代碼對照表");
        CodeDescription existing = codeTableCacheService.findCode(codeGroup, codeField, codeBefore);
        if (existing == null) {
            throw new NoSuchElementException("找不到要刪除的代碼: " + uniqueKey);
        }
        saveReview("DELETE", existing, existing, null, username);
    }

    private void saveReview(String operation, CodeDescription code, CodeDescription before,
                            CodeDescription after, String username) {
        if ("CREATE".equals(operation) && after != null && after.getCodeDefinitionId() == null) {
            after.setCodeDefinitionId(UuidV7.next());
        }
        CodeDescription keySource = before == null ? code : before;
        String uniqueKey = keySource.getCodeGroup() + "|" + keySource.getCodeField() + "|" + keySource.getCodeBefore();
        String beforeText = serializeReviewContent(before);
        String afterText = serializeReviewContent(after);
        String reviewKey = CODE_TABLE_FUNCTION_CODE + "|" + uniqueKey + "|" + UUID.randomUUID();
        ChangeReview review = ChangeReview.builder()
                .reviewUuid(UuidV7.next()).operation(operation).workflowMode("STAGED")
                .sourceType("CODE_DEFINITION").sourceRecordType("CODE_DEFINITION")
                .sourceRecordId((after == null ? before : after).getCodeDefinitionId())
                .functionCode(CODE_TABLE_FUNCTION_CODE).uniqueKey(uniqueKey).reviewKey(reviewKey)
                .key1(code.getCodeGroup())
                .contentBefore(beforeText).contentAfter(afterText)
                .reviewStatus("P")
                .createdBy(username).createdAt(LocalDateTime.now()).build();
        pendingReviewGuard.acquire(CODE_TABLE_FUNCTION_CODE, uniqueKey, reviewKey, username, "代碼對照表");
        String targetKey = code.getCodeGroup() + "|" + code.getCodeField() + "|" + code.getCodeBefore();
        if (!targetKey.equals(uniqueKey)) {
            pendingReviewGuard.acquire(CODE_TABLE_FUNCTION_CODE, targetKey, reviewKey, username, "代碼對照表目標 Key");
        }
        if (reviewExecutionPolicy.isDirectCompletion(username)) {
            changeReviewApplier.apply(review, username);
            changeReviewService.recordDirectCompletion(review, username);
        } else {
            if (policyChangeDao.insertChangeReview(review) != 1) {
                throw new IllegalStateException("新增覆核資料失敗");
            }
            changeReviewService.recordSubmission(review);
        }
    }

    private String serializeReviewContent(CodeDescription content) {
        if (content == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("無法建立代碼覆核內容快照", exception);
        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"codeTableCodes", "codeTableCode", "codeTableCodesByGroup",
            "apiFunctionCodes", "availableFunctionCodes"}, allEntries = true)
    public CodeDescription reviewCode(String codeGroup, String codeField, String codeBefore, String reviewedBy) {
        String uniqueKey = codeGroup + "|" + codeField + "|" + codeBefore;
        ChangeReview review = policyChangeDao.findPendingChangeReviewForUpdate(CODE_TABLE_FUNCTION_CODE, uniqueKey);
        if (review == null) {
            throw new NoSuchElementException("找不到待處理的代碼覆核資料: " + uniqueKey);
        }
        changeReviewService.decide(review.getReviewKey(), "S", null, reviewedBy);
        return policyChangeDao.findCode(codeGroup, codeField, codeBefore);
    }

    @Override
    public List<CodeDescription> findAddressTypes() {
        return findCodes(CodeTable.ADDRESS_TYPE);
    }

    @Override
    public List<CodeDescription> findAcceptanceStatuses() {
        return findCodes(CodeTable.ACCEPTANCE_STATUS);
    }

    @Override
    public List<CodeDescription> findChangeItems() {
        return findCodes(CodeTable.CHANGE_ITEM);
    }

    @Override
    public List<CodeDescription> findScreenPermissions() {
        return findCodes(CodeTable.SCREEN_PERMISSION);
    }

    @Override
    public List<CodeDescription> findScreenFunctionCodes() {
        return findCodes(CodeTable.SCREEN_FUNCTION);
    }

    @Override
    public List<CodeDescription> findNavigationLabels() {
        return findCodes(CodeTable.NAVIGATION_LABEL);
    }

    @Override
    public List<CodeDescription> findUserAuthorizationPermissions() {
        return findCodes(CodeTable.USER_AUTHORIZATION);
    }

    @Override
    public CodeDescription findPostalCodeZipCode3(String zipCode3) {
        return codeTableCacheService.findCode(
                CodeTable.POSTAL_CODE_ZIP_CODE3.getCodeGroup(),
                CodeTable.POSTAL_CODE_ZIP_CODE3.getCodeField(),
                zipCode3
        );
    }

    @Override
    public Map<String, String> findChtFieldNames() {
        Map<String, String> fieldNames = new LinkedHashMap<>();
        for (CodeDescription code : codeTableCacheService.findCodesByGroup("CHT-code")) {
            // 新版 DD 以 codeDescription 保存中文名稱；相容舊資料時才退回 codeBefore。
            String label = code.getCodeDescription() == null || code.getCodeDescription().isBlank()
                    ? code.getCodeBefore()
                    : code.getCodeDescription();
            fieldNames.putIfAbsent(code.getCodeField(), label);
        }
        return fieldNames;
    }

    @Override
    public String communicationAddressCode() {
        return codeBefore(CodeDescriptionMeaning.COMMUNICATION_ADDRESS);
    }

    @Override
    public Optional<String> findCommunicationAddressCode() {
        return findCodes(CodeDescriptionMeaning.COMMUNICATION_ADDRESS.getCodeTable()).stream()
                .filter(code -> CodeDescriptionMeaning.COMMUNICATION_ADDRESS.getCodeBefore().equals(code.getCodeBefore()))
                .map(CodeDescription::getCodeBefore)
                .findFirst();
    }

    @Override
    public String registeredAddressCode() {
        return codeBefore(CodeDescriptionMeaning.REGISTERED_ADDRESS);
    }

    @Override
    public String emailAddressCode() {
        return codeBefore(CodeDescriptionMeaning.EMAIL_ADDRESS);
    }

    @Override
    public String addressChangeItemCode() {
        return changeItemCode(CodeDescriptionMeaning.ADDRESS_CHANGE);
    }

    @Override
    public String mainAmountChangeItemCode() {
        return changeItemCode(CodeDescriptionMeaning.MAIN_AMOUNT_CHANGE);
    }

    @Override
    public String riderAmountChangeItemCode() {
        return changeItemCode(CodeDescriptionMeaning.RIDER_AMOUNT_CHANGE);
    }

    @Override
    public String emailChangeItemCode() {
        return changeItemCode(CodeDescriptionMeaning.EMAIL_CHANGE);
    }

    @Override
    public String telephoneChangeItemCode() {
        return changeItemCode(CodeDescriptionMeaning.TELEPHONE_CHANGE);
    }

    @Override
    public String mobileChangeItemCode() {
        return changeItemCode(CodeDescriptionMeaning.MOBILE_CHANGE);
    }

    @Override
    public String pendingStatusCode() {
        return acceptanceStatusCode(CodeDescriptionMeaning.PENDING_STATUS);
    }

    @Override
    public String processingStatusCode() {
        return acceptanceStatusCode(CodeDescriptionMeaning.PROCESSING_STATUS);
    }

    @Override
    public String completeStatusCode() {
        return acceptanceStatusCode(CodeDescriptionMeaning.COMPLETE_STATUS);
    }

    @Override
    public String cancelStatusCode() {
        return acceptanceStatusCode(CodeDescriptionMeaning.CANCEL_STATUS);
    }

    @Override
    public String mainRideTypeCode() {
        return codeBefore(CodeDescriptionMeaning.MAIN_RIDE_TYPE);
    }

    private List<CodeDescription> findCodes(CodeTable codeTable) {
        return codeTableCacheService.findCodes(codeTable.getCodeGroup(), codeTable.getCodeField());
    }

    private String changeItemCode(CodeDescriptionMeaning meaning) {
        return codeBefore(meaning);
    }

    private String acceptanceStatusCode(CodeDescriptionMeaning meaning) {
        return codeBefore(meaning);
    }

    private String codeBefore(CodeDescriptionMeaning meaning) {
        return findCodes(meaning.getCodeTable()).stream()
                .filter(code -> meaning.getCodeBefore().equals(code.getCodeBefore()))
                .map(CodeDescription::getCodeBefore)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "找不到代碼: " + meaning.getCodeTable().getCodeGroup() + "/" + meaning.getCodeTable().getCodeField() + "/" + meaning.getCodeBefore()
                ));
    }
}
