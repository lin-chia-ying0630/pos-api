package com.alin.lin.service;

import com.alin.lin.entity.ChangeReview;

/**
 * 將已核准的 staging 快照套用至正式資料。
 * 實作由呼叫端既有交易包覆，確保正式表與覆核狀態共同成功或回滾。
 */
public interface ChangeReviewApplier {
    void apply(ChangeReview review, String operatorId);
}
