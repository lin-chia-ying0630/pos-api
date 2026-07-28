package com.alin.lin.service;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.entity.ChangeReview;
import com.alin.lin.entity.ChangeReviewAudit;
import com.alin.lin.exception.ChangeCaseConflictException;
import com.alin.lin.service.impl.ChangeReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChangeReviewServiceImplTest {
    private PolicyChangeDao dao;
    private ChangeReviewServiceImpl service;
    private ChangeReviewApplier applier;

    @BeforeEach
    void setUp() {
        dao = mock(PolicyChangeDao.class);
        applier = mock(ChangeReviewApplier.class);
        service = new ChangeReviewServiceImpl(dao, applier);
    }

    @Test
    void recordsSubmissionAsAppendOnlyAuditEvent() {
        ChangeReview review = pendingReview();
        when(dao.insertChangeReviewAudit(any())).thenReturn(1);

        service.recordSubmission(review);

        ArgumentCaptor<ChangeReviewAudit> captor = ArgumentCaptor.forClass(ChangeReviewAudit.class);
        verify(dao).insertChangeReviewAudit(captor.capture());
        assertThat(captor.getValue().getAction()).isEqualTo("SUBMIT");
        assertThat(captor.getValue().getStatusBefore()).isNull();
        assertThat(captor.getValue().getStatusAfter()).isEqualTo("P");
        assertThat(captor.getValue().getOperatorId()).isEqualTo("maker-a");
        assertThat(captor.getValue().getEventId()).isNotBlank();
    }

    @Test
    void returnsTwentyNewestReviewsPerPageWithoutRequiredFilters() {
        when(dao.countChangeReviews("", "", "")).thenReturn(45L);
        when(dao.findChangeReviews("", "", "", 20, 20)).thenReturn(java.util.List.of(pendingReview()));

        var result = service.findReviews("", "", "", 2);

        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getPageSize()).isEqualTo(20);
        assertThat(result.getTotalItems()).isEqualTo(45);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getItems()).hasSize(1);
        verify(dao).findChangeReviews("", "", "", 20, 20);
    }

    @Test
    void filtersReviewsByNormalizedReviewStatus() {
        when(dao.countChangeReviews("", "", "P")).thenReturn(1L);
        when(dao.findChangeReviews("", "", "P", 20, 0)).thenReturn(java.util.List.of(pendingReview()));

        var result = service.findReviews("", "", "p", 1);

        assertThat(result.getTotalItems()).isEqualTo(1);
        verify(dao).findChangeReviews("", "", "P", 20, 0);
    }

    @Test
    void rejectsUnknownReviewStatusFilter() {
        assertThatThrownBy(() -> service.findReviews("", "", "X", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("覆核狀態只允許 P、S、C");
    }

    @Test
    void approvesPendingReviewAndAppendsAuditEvent() {
        when(dao.findChangeReviewForUpdate("review-1")).thenReturn(pendingReview());
        when(dao.updateChangeReviewStatus("review-1", "S", null, "reviewer-a")).thenReturn(1);
        when(dao.insertChangeReviewAudit(any())).thenReturn(1);

        service.decide("review-1", "S", null, "reviewer-a");

        ArgumentCaptor<ChangeReviewAudit> captor = ArgumentCaptor.forClass(ChangeReviewAudit.class);
        verify(dao).insertChangeReviewAudit(captor.capture());
        assertThat(captor.getValue().getAction()).isEqualTo("APPROVE");
        assertThat(captor.getValue().getStatusBefore()).isEqualTo("P");
        assertThat(captor.getValue().getStatusAfter()).isEqualTo("S");
    }

    @Test
    void appliesStagedContentBeforeMarkingReviewCompleted() {
        ChangeReview review = pendingReview();
        review.setWorkflowMode("STAGED");
        review.setSourceType("POLICY_CONTRACT");
        review.setUniqueKey("P000000001|1");
        when(dao.findChangeReviewForUpdate("review-1")).thenReturn(review);
        when(dao.updateChangeReviewStatus("review-1", "S", null, "reviewer-a")).thenReturn(1);
        when(dao.insertChangeReviewAudit(any())).thenReturn(1);

        service.decide("review-1", "S", null, "reviewer-a");

        verify(applier).apply(review, "reviewer-a");
        verify(dao).releasePendingReviewLock("review-1");
    }

    @Test
    void rejectsSelfReviewWithoutChangingState() {
        when(dao.findChangeReviewForUpdate("review-1")).thenReturn(pendingReview());

        assertThatThrownBy(() -> service.decide("review-1", "S", null, "maker-a"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("不可覆核自己的異動");

        verify(dao, never()).updateChangeReviewStatus(any(), any(), any(), any());
        verify(dao, never()).insertChangeReviewAudit(any());
    }

    @Test
    void rejectsDecisionWhenReviewWasAlreadyHandled() {
        ChangeReview review = pendingReview();
        review.setReviewStatus("S");
        when(dao.findChangeReviewForUpdate("review-1")).thenReturn(review);

        assertThatThrownBy(() -> service.decide("review-1", "S", null, "reviewer-a"))
                .isInstanceOf(ChangeCaseConflictException.class);
    }

    private ChangeReview pendingReview() {
        return ChangeReview.builder()
                .id(1L)
                .reviewKey("review-1")
                .functionCode("MCM00001")
                .reviewStatus("P")
                .createdBy("maker-a")
                .contentBefore("before")
                .contentAfter("after")
                .build();
    }
}
