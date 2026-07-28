package com.alin.lin.dao;

import com.alin.lin.entity.UserAccountSecurityRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserAccountSecurityDao {
    UserAccountSecurityRecord findByUserId(@Param("userId") String userId);

    int countByUserId(@Param("userId") String userId);

    List<String> findRoleCodesByUserId(@Param("userId") String userId);

    int insertUser(@Param("userId") String userId,
                   @Param("password") String password,
                   @Param("enabled") boolean enabled);

    int updateUser(@Param("userId") String userId,
                   @Param("password") String password,
                   @Param("enabled") boolean enabled);

    int deleteUser(@Param("userId") String userId);

    int changePassword(@Param("userId") String userId,
                       @Param("password") String password);

    int insertAuthority(@Param("userId") String userId,
                        @Param("roleCode") String roleCode);

    int deleteAuthorities(@Param("userId") String userId);
}
