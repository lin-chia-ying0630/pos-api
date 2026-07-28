package com.alin.lin.service.policy;

import com.alin.lin.dao.UserRoleAuthorizationDao;
import org.springframework.stereotype.Component;

/** 決定異動是否需要覆核；Admin 直接完成但仍必須產生 S 稽核軌跡。 */
@Component
public class ReviewExecutionPolicy {
    private final UserRoleAuthorizationDao userRoleAuthorizationDao;

    public ReviewExecutionPolicy(UserRoleAuthorizationDao userRoleAuthorizationDao) {
        this.userRoleAuthorizationDao = userRoleAuthorizationDao;
    }

    public boolean isDirectCompletion(String userId) {
        return userRoleAuthorizationDao.existsAdminRoleAssignment(userId);
    }
}
