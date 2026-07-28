package com.alin.lin.service;

import com.alin.lin.dao.UserRoleAuthorizationDao;
import com.alin.lin.service.policy.ReviewExecutionPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReviewExecutionPolicyTest {
    @Test
    void returnsTrueWhenUserHasAdminRoleAssignment() {
        UserRoleAuthorizationDao dao = mock(UserRoleAuthorizationDao.class);
        when(dao.existsAdminRoleAssignment("adminUser")).thenReturn(true);

        ReviewExecutionPolicy policy = new ReviewExecutionPolicy(dao);

        assertThat(policy.isDirectCompletion("adminUser")).isTrue();
    }

    @Test
    void returnsFalseWhenUserDoesNotHaveAdminRoleAssignment() {
        UserRoleAuthorizationDao dao = mock(UserRoleAuthorizationDao.class);
        when(dao.existsAdminRoleAssignment("normalUser")).thenReturn(false);

        ReviewExecutionPolicy policy = new ReviewExecutionPolicy(dao);

        assertThat(policy.isDirectCompletion("normalUser")).isFalse();
    }
}
