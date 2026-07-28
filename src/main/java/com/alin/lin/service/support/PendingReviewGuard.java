package com.alin.lin.service.support;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.entity.ChangeReview;
import com.alin.lin.exception.ChangeCaseConflictException;
import org.springframework.stereotype.Component;
import org.springframework.dao.DuplicateKeyException;

@Component
public class PendingReviewGuard {
    private final PolicyChangeDao policyChangeDao;

    public PendingReviewGuard(PolicyChangeDao policyChangeDao) {
        this.policyChangeDao = policyChangeDao;
    }

    public void requireNoPending(String functionCode, String uniqueKey, String displayName) {
        ChangeReview pending = policyChangeDao.findPendingChangeReviewForUpdate(functionCode, uniqueKey);
        if (pending != null) {
            throw new ChangeCaseConflictException(displayName + "已有相同 Key 的資料正在處理中，請先完成既有佇列資料");
        }
    }

    /** 以資料庫複合主鍵消除「查無資料後同時新增」的併發空窗。 */
    public void acquire(String functionCode, String uniqueKey, String reviewKey, String userId, String displayName) {
        try {
            policyChangeDao.acquirePendingReviewLock(functionCode, uniqueKey, reviewKey, userId);
        } catch (DuplicateKeyException exception) {
            throw new ChangeCaseConflictException(displayName + "已有相同 Key 的資料正在處理中，請先完成既有佇列資料");
        }
    }
}
