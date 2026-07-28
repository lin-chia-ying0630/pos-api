package com.alin.lin.service;

import com.alin.lin.entity.UserAccountSecurityRecord;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Set;

public interface UserAccountSecurityService {
    UserDetails loadUserByUsername(String username);

    boolean userExists(String username);

    void createUser(UserDetails user);

    void updateUser(UserDetails user);

    void deleteUser(String username);

    void changePassword(String username, String password);

    void createAuthority(String username, String authority);

    void deleteUserAuthorities(String username);

    List<String> findAuthorities(String username);

    UserAccountSecurityRecord findSecurityRecord(String username);

    /**
     * 將環境設定的系統帳號同步至資料庫。
     * 設定層只提供帳號資料，查詢、新增、更新與角色重建均由 Service 交易統一處理。
     */
    void synchronizeConfiguredUser(String userId, String encodedPassword, Set<String> authorities);
}
