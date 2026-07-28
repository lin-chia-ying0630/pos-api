package com.alin.lin.service.impl;

import com.alin.lin.dao.UserAccountSecurityDao;
import com.alin.lin.entity.UserAccountSecurityRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.inOrder;

class UserAccountSecurityServiceImplTest {
    private UserAccountSecurityDao dao;
    private UserAccountSecurityServiceImpl service;

    @BeforeEach
    void setUp() {
        dao = mock(UserAccountSecurityDao.class);
        service = new UserAccountSecurityServiceImpl(dao);
    }

    @Test
    void loadUserByUsernameBuildsUserDetailsFromDaoRecord() {
        when(dao.findByUserId("alice")).thenReturn(new UserAccountSecurityRecord("alice", "encoded", true));
        when(dao.findRoleCodesByUserId("alice")).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));

        var userDetails = service.loadUserByUsername("alice");

        assertThat(userDetails.getUsername()).isEqualTo("alice");
        assertThat(userDetails.getPassword()).isEqualTo("encoded");
        assertThat(userDetails.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void userExistsReturnsTrueWhenCountIsPositive() {
        when(dao.countByUserId("alice")).thenReturn(1);

        assertThat(service.userExists("alice")).isTrue();
    }

    @Test
    void userExistsReturnsFalseWhenCountIsZero() {
        when(dao.countByUserId("bob")).thenReturn(0);

        assertThat(service.userExists("bob")).isFalse();
    }

    @Test
    void createUserDelegatesToDao() {
        UserDetails user = User.withUsername("bob").password("secret").roles("USER").build();

        service.createUser(user);

        verify(dao).insertUser("bob", "secret", true);
    }

    @Test
    void updateUserDelegatesToDao() {
        UserDetails user = User.withUsername("bob").password("secret").roles("USER").build();

        service.updateUser(user);

        verify(dao).updateUser("bob", "secret", true);
    }

    @Test
    void deleteUserDelegatesToDao() {
        service.deleteUser("bob");

        verify(dao).deleteUser("bob");
    }

    @Test
    void changePasswordDelegatesToDao() {
        service.changePassword("bob", "newPassword");

        verify(dao).changePassword("bob", "newPassword");
    }

    @Test
    void createAuthorityDelegatesToDao() {
        service.createAuthority("bob", "ROLE_USER");

        verify(dao).insertAuthority("bob", "ROLE_USER");
    }

    @Test
    void deleteUserAuthoritiesDelegatesToDao() {
        service.deleteUserAuthorities("bob");

        verify(dao).deleteAuthorities("bob");
    }

    @Test
    void findAuthoritiesDelegatesToDao() {
        when(dao.findRoleCodesByUserId("bob")).thenReturn(List.of("ROLE_USER"));

        assertThat(service.findAuthorities("bob")).containsExactly("ROLE_USER");
    }

    @Test
    void findSecurityRecordDelegatesToDao() {
        UserAccountSecurityRecord record = new UserAccountSecurityRecord("bob", "secret", false);
        when(dao.findByUserId("bob")).thenReturn(record);

        assertThat(service.findSecurityRecord("bob")).isSameAs(record);
    }

    @Test
    void synchronizeConfiguredUserKeepsExistingAuthoritiesAndRebuildsThemInOneServiceFlow() {
        when(dao.countByUserId("reviewer")).thenReturn(1);
        when(dao.findRoleCodesByUserId("reviewer")).thenReturn(List.of("ROLE_ADMIN"));

        service.synchronizeConfiguredUser("reviewer", "encoded", Set.of("ROLE_REVIEWER"));

        var ordered = inOrder(dao);
        ordered.verify(dao).updateUser("reviewer", "encoded", true);
        ordered.verify(dao).deleteAuthorities("reviewer");
        verify(dao).insertAuthority("reviewer", "ROLE_ADMIN");
        verify(dao).insertAuthority("reviewer", "ROLE_REVIEWER");
    }
}
