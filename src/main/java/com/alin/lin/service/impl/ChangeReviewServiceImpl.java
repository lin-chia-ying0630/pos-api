package com.alin.lin.service.impl;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.entity.ChangeReview;
import com.alin.lin.entity.ChangeReviewAudit;
import com.alin.lin.dto.ChangeReviewPageDto;
import com.alin.lin.exception.ChangeCaseConflictException;
import com.alin.lin.service.ChangeReviewService;
import com.alin.lin.service.ChangeReviewApplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.slf4j.MDC;

import com.alin.lin.enums.ReviewSourceType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import com.alin.lin.util.UuidV7;

@Service
public class ChangeReviewServiceImpl implements ChangeReviewService {
    private static final int PAGE_SIZE = 20;
    private final PolicyChangeDao policyChangeDao;
    private final ChangeReviewApplier changeReviewApplier;

    public ChangeReviewServiceImpl(PolicyChangeDao policyChangeDao, ChangeReviewApplier changeReviewApplier) {
        this.policyChangeDao = policyChangeDao;
        this.changeReviewApplier = changeReviewApplier;
    }

    @Override
    @Transactional(readOnly = true)
    public ChangeReviewPageDto findReviews(String functionCode, String key1, String reviewStatus, int page) {
        String normalizedReviewStatus = normalizeReviewStatus(reviewStatus);
        int normalizedPage = Math.max(page, 1);
        long totalItems = policyChangeDao.countChangeReviews(functionCode, key1, normalizedReviewStatus);
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / PAGE_SIZE);
        if (totalPages > 0) {
            normalizedPage = Math.min(normalizedPage, totalPages);
        }
        List<ChangeReview> items = policyChangeDao.findChangeReviews(
                functionCode,
                key1,
                normalizedReviewStatus,
                PAGE_SIZE,
                (normalizedPage - 1) * PAGE_SIZE
        );
        return ChangeReviewPageDto.builder()
                .items(items)
                .page(normalizedPage)
                .pageSize(PAGE_SIZE)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .build();
    }

    private String normalizeReviewStatus(String reviewStatus) {
        if (reviewStatus == null || reviewStatus.isBlank()) return "";
        String normalized = reviewStatus.trim().toUpperCase();
        if (!List.of("P", "S", "C").contains(normalized)) {
            throw new IllegalArgumentException("覆核狀態只允許 P、S、C");
        }
        return normalized;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChangeReviewAudit> findAuditTrail(String reviewKey) {
        return policyChangeDao.findChangeReviewAudits(reviewKey);
    }

    @Override
    @Transactional
    public void recordSubmission(ChangeReview review) {
        if (review.getId() == null) {
            throw new IllegalStateException("覆核主檔尚未取得識別碼，無法建立稽核軌跡");
        }
        insertAudit(review, "SUBMIT", null, "P", review.getCreatedBy(), null);
    }

    @Override
    @Transactional
    public void recordDirectCompletion(ChangeReview review, String operatorId) {
        if (review.getReviewUuid() == null) review.setReviewUuid(UuidV7.next());
        review.setWorkflowMode("DIRECT");
        review.setReviewStatus("S");
        review.setReviewedBy(operatorId);
        if (policyChangeDao.insertCompletedChangeReview(review) != 1 || review.getId() == null) {
            throw new IllegalStateException("建立直接完成稽核主檔失敗");
        }
        insertAudit(review, "DIRECT_APPLY", null, "S", operatorId, "Admin 直接完成異動");
        policyChangeDao.releasePendingReviewLock(review.getReviewKey());
    }

    @Override
    @Transactional
    public void decide(String reviewKey, String status, String reviewRemark, String reviewedBy) {
        String targetStatus = normalizeDecision(status, reviewRemark);
        ChangeReview review = policyChangeDao.findChangeReviewForUpdate(reviewKey);
        if (review == null) {
            throw new NoSuchElementException("找不到覆核資料: " + reviewKey);
        }
        if (!"P".equals(review.getReviewStatus())) {
            throw new ChangeCaseConflictException("覆核資料已由其他人處理，請重新查詢");
        }
        if (reviewedBy.equals(review.getCreatedBy())) {
            throw new AccessDeniedException("建檔人員不可覆核自己的異動");
        }
        // 新流程只在核准時套用正式資料；舊資料沿用狀態同步，避免重複套用歷史快照。
        if ("S".equals(targetStatus) && "STAGED".equals(review.getWorkflowMode())) {
            changeReviewApplier.apply(review, reviewedBy);
        }
        int updated = policyChangeDao.updateChangeReviewStatus(reviewKey, targetStatus, reviewRemark, reviewedBy);
        if (updated != 1) {
            throw new ChangeCaseConflictException("覆核資料狀態已變更，請重新查詢");
        }
        if (!"STAGED".equals(review.getWorkflowMode())) {
            synchronizeSourceReviewStatus(review, targetStatus, reviewedBy);
        }
        insertAudit(
                review,
                "S".equals(targetStatus) ? "APPROVE" : "REJECT",
                "P",
                targetStatus,
                reviewedBy,
                reviewRemark
        );
        policyChangeDao.releasePendingReviewLock(review.getReviewKey());
    }

    private void synchronizeSourceReviewStatus(ChangeReview review, String targetStatus, String reviewedBy) {
        if (ReviewSourceType.isPolicyContract(review.getSourceType())) {
            int updated = policyChangeDao.updatePolicyMasterReviewDecision(
                    review.getPolicyNo(), review.getPolicySeq(), targetStatus, reviewedBy);
            if (updated != 1) {
                throw new IllegalStateException("更新保單主檔覆核狀態失敗");
            }
            return;
        }
        if (ReviewSourceType.isCodeDefinition(review.getSourceType())) {
            String[] keyParts = review.getUniqueKey() == null ? new String[0] : review.getUniqueKey().split("\\|", -1);
            if (keyParts.length != 3) {
                throw new IllegalStateException("代碼對照表覆核鍵值格式錯誤");
            }
            int updated = policyChangeDao.updateCodeReviewDecision(
                    keyParts[0], keyParts[1], keyParts[2], targetStatus, reviewedBy);
            if (updated != 1) {
                throw new IllegalStateException("更新代碼對照表覆核狀態失敗");
            }
            return;
        }
        String[] keyParts = review.getUniqueKey() == null ? new String[0] : review.getUniqueKey().split("\\|", -1);
        if (ReviewSourceType.isPolicyContact(review.getSourceType())) {
            if (keyParts.length != 3 || policyChangeDao.updatePolicyAddressReviewDecision(
                    keyParts[0], Integer.valueOf(keyParts[1]), keyParts[2], targetStatus, reviewedBy) != 1) {
                throw new IllegalStateException("更新保單地址覆核狀態失敗");
            }
            return;
        }
        if (ReviewSourceType.isPolicyCoverage(review.getSourceType())) {
            if (keyParts.length != 3 || policyChangeDao.updatePolicyRideReviewDecision(
                    keyParts[0], Integer.valueOf(keyParts[1]), keyParts[2], targetStatus, reviewedBy) != 1) {
                throw new IllegalStateException("更新保單主附約覆核狀態失敗");
            }
        }
    }

    private String normalizeDecision(String status, String reviewRemark) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!"S".equals(normalized) && !"C".equals(normalized)) {
            throw new IllegalArgumentException("覆核狀態只能是 S 或 C");
        }
        if ("C".equals(normalized) && (reviewRemark == null || reviewRemark.isBlank())) {
            throw new IllegalArgumentException("拒絕時必須填寫說明");
        }
        return normalized;
    }

    private void insertAudit(
            ChangeReview review,
            String action,
            String statusBefore,
            String statusAfter,
            String operatorId,
            String reviewRemark
    ) {
        int inserted = policyChangeDao.insertChangeReviewAudit(ChangeReviewAudit.builder()
                .eventId(UuidV7.next())
                .reviewId(review.getId())
                .reviewKey(review.getReviewKey())
                .functionCode(review.getFunctionCode())
                .action(action)
                .statusBefore(statusBefore)
                .statusAfter(statusAfter)
                .operatorId(operatorId)
                .reviewRemark(reviewRemark)
                .contentBefore(review.getContentBefore())
                .contentAfter(review.getContentAfter())
                .requestId(MDC.get("requestId"))
                .traceId(MDC.get("traceId"))
                .occurredAt(LocalDateTime.now())
                .build());
        if (inserted != 1) {
            throw new IllegalStateException("新增覆核稽核軌跡失敗");
        }
    }
}
