package com.alin.lin.service;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.dao.UserRoleAuthorizationDao;
import com.alin.lin.dto.UserRoleAuthorizationRequest;
import com.alin.lin.entity.ChangeReview;
import com.alin.lin.exception.ChangeCaseConflictException;
import com.alin.lin.service.impl.UserRoleAuthorizationServiceImpl;
import com.alin.lin.service.support.PendingReviewGuard;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserRoleAuthorizationServiceImplTest {
    private final UserRoleAuthorizationDao userRoleAuthorizationDao = mock(UserRoleAuthorizationDao.class);
    private final PolicyChangeDao policyChangeDao = mock(PolicyChangeDao.class);
    private final PendingReviewGuard pendingReviewGuard = new PendingReviewGuard(policyChangeDao);
    private final UserRoleAuthorizationServiceImpl service =
            new UserRoleAuthorizationServiceImpl(userRoleAuthorizationDao, policyChangeDao, new ObjectMapper(), pendingReviewGuard);

    @Test
    void rejectsUpdateWhenSameBusinessKeyIsAlreadyPending() {
        when(policyChangeDao.findPendingChangeReviewForUpdate("MUS00001", "reviewer"))
                .thenReturn(ChangeReview.builder().id(99L).reviewStatus("P").build());
        UserRoleAuthorizationRequest request = UserRoleAuthorizationRequest.builder()
                .userId("reviewer")
                .roles(List.of("REVIEWER", "ADMIN"))
                .build();

        ChangeCaseConflictException exception = assertThrows(ChangeCaseConflictException.class,
                () -> service.replaceRoles(request, "admin"));

        assertEquals("使用者 reviewer 的角色異動已有相同 Key 的資料正在處理中，請先完成既有佇列資料", exception.getMessage());
        verifyNoInteractions(userRoleAuthorizationDao);
    }

    @Test
    void readsUserAuditTimeReturnedAsLocalDateTime() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 28, 20, 30);
        when(userRoleAuthorizationDao.findAllUserAccounts()).thenReturn(List.of(Map.of(
                "user_id", "admin",
                "enabled", true,
                "created_by", "system",
                "created_at", createdAt,
                "updated_by", "system",
                "updated_at", createdAt
        )));
        when(userRoleAuthorizationDao.findAllRoleAssignments()).thenReturn(List.of());
        when(userRoleAuthorizationDao.findLatestReviewStatuses("MUS00001")).thenReturn(List.of());

        assertEquals(createdAt, service.findAll().get(0).getCreatedAt());
    }

    @Test
    void readsScreenAuditTimeReturnedAsSqlTimestamp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 28, 20, 45);
        when(userRoleAuthorizationDao.findAllUserAccounts())
                .thenReturn(List.of(Map.of("user_id", "admin")));
        when(userRoleAuthorizationDao.findAllScreenAuthorizationRows()).thenReturn(List.of(Map.of(
                "user_id", "admin",
                "function_code", "MUS00001",
                "created_by", "system",
                "created_at", Timestamp.valueOf(createdAt),
                "updated_by", "system",
                "updated_at", Timestamp.valueOf(createdAt)
        )));

        assertEquals(createdAt, service.findAllScreenAuthorizations().get(0).getCreatedAt());
    }
}
