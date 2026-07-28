package com.alin.lin.service;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.exception.ChangeCaseConflictException;
import com.alin.lin.service.support.PendingReviewGuard;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class PendingReviewGuardTest {
    @Test
    void convertsDatabaseUniqueLockConflictToBusinessConflict() {
        PolicyChangeDao dao = mock(PolicyChangeDao.class);
        doThrow(new DuplicateKeyException("duplicate")).when(dao)
                .acquirePendingReviewLock("MPM00001", "P000000001|1", "review-1", "maker");

        PendingReviewGuard guard = new PendingReviewGuard(dao);

        assertThatThrownBy(() -> guard.acquire(
                "MPM00001", "P000000001|1", "review-1", "maker", "保單主檔"))
                .isInstanceOf(ChangeCaseConflictException.class)
                .hasMessageContaining("正在處理中");
    }
}
