package com.alin.lin.service.impl;

import com.alin.lin.dao.UserAccountSecurityDao;
import com.alin.lin.entity.UserAccountSecurityRecord;
import com.alin.lin.service.UserAccountSecurityService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAccountSecurityServiceImpl implements UserAccountSecurityService {
    // DAO 是此 Service 唯一的資料存取入口，SecurityConfig 不得直接操作帳號資料表。
    private final UserAccountSecurityDao userAccountSecurityDao;

    public UserAccountSecurityServiceImpl(UserAccountSecurityDao userAccountSecurityDao) {
        this.userAccountSecurityDao = userAccountSecurityDao;
    }

    @Override
    /*
     * 不快取 Spring Security 的可變 UserDetails：
     * 驗證成功後 Provider 會清除其中的密碼；若快取同一物件，下一個 API 請求會取得空密碼而誤判 401。
     * 畫面功能代碼等不含憑證的資料仍可獨立快取。
     */
    public UserDetails loadUserByUsername(String username) {
        UserAccountSecurityRecord record = findSecurityRecord(username);
        List<GrantedAuthority> authorities = findAuthorities(username).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return User.withUsername(record.userId())
                .password(record.password())
                .authorities(authorities)
                .disabled(!record.enabled())
                .build();
    }

    @Override
    public boolean userExists(String username) {
        return userAccountSecurityDao.countByUserId(username) > 0;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "userSecurityDetails", key = "#user.username")
    public void createUser(UserDetails user) {
        userAccountSecurityDao.insertUser(user.getUsername(), user.getPassword(), true);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "userSecurityDetails", key = "#user.username")
    public void updateUser(UserDetails user) {
        userAccountSecurityDao.updateUser(user.getUsername(), user.getPassword(), true);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "userSecurityDetails", key = "#username")
    public void deleteUser(String username) {
        userAccountSecurityDao.deleteUser(username);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "userSecurityDetails", key = "#username")
    public void changePassword(String username, String password) {
        userAccountSecurityDao.changePassword(username, password);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "userSecurityDetails", key = "#username")
    public void createAuthority(String username, String authority) {
        userAccountSecurityDao.insertAuthority(username, authority);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "userSecurityDetails", key = "#username")
    public void deleteUserAuthorities(String username) {
        userAccountSecurityDao.deleteAuthorities(username);
    }

    @Override
    public List<String> findAuthorities(String username) {
        return userAccountSecurityDao.findRoleCodesByUserId(username);
    }

    @Override
    public UserAccountSecurityRecord findSecurityRecord(String username) {
        return userAccountSecurityDao.findByUserId(username);
    }

    @Override
    @Transactional
    // 密碼與角色同步完成後清除此帳號快取，下一次登入一定重新讀取最新授權。
    @CacheEvict(cacheNames = "userSecurityDetails", key = "#userId")
    public void synchronizeConfiguredUser(String userId, String encodedPassword, Set<String> configuredAuthorities) {
        Set<String> authorities = new LinkedHashSet<>(configuredAuthorities);
        if (userExists(userId)) {
            // 保留管理畫面已配置的其他角色，再合併環境設定要求的基本角色。
            authorities.addAll(findAuthorities(userId));
            userAccountSecurityDao.updateUser(userId, encodedPassword, true);
        } else {
            userAccountSecurityDao.insertUser(userId, encodedPassword, true);
        }
        userAccountSecurityDao.deleteAuthorities(userId);
        authorities.forEach(authority -> userAccountSecurityDao.insertAuthority(userId, authority));
    }
}
