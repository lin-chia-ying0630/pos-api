package com.alin.lin.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserRoleAuthorizationDao {
    List<Map<String, Object>> findAllUserAccounts();

    List<Map<String, Object>> findAllRoleAssignments();

    int countByUserId(@Param("userId") String userId);

    boolean findEnabledByUserId(@Param("userId") String userId);

    int countOtherEnabledAdmins(@Param("userId") String userId);

    void insertUserAccount(@Param("id") String id,
                           @Param("userId") String userId,
                           @Param("password") String password,
                           @Param("enabled") boolean enabled,
                           @Param("createdBy") String createdBy,
                           @Param("updatedBy") String updatedBy);

    void insertUserRoleAssignment(@Param("id") String id,
                                 @Param("userId") String userId,
                                 @Param("roleCode") String roleCode,
                                 @Param("createdBy") String createdBy,
                                 @Param("updatedBy") String updatedBy);

    void deleteUserRoleAssignments(@Param("userId") String userId);

    void updateUserAccountEnabled(@Param("userId") String userId,
                                  @Param("enabled") boolean enabled,
                                  @Param("updatedBy") String updatedBy);

    List<String> findRoleCodesByUserId(@Param("userId") String userId);

    List<Map<String, Object>> findLatestReviewStatuses(@Param("functionCode") String functionCode);

    List<Map<String, Object>> findAllScreenAuthorizationRows();

    List<Map<String, Object>> findScreenAuthorizationRows(@Param("userId") String userId);

    List<String> findFunctionCodes(@Param("userId") String userId);

    List<String> findAvailableFunctionCodes();

    void deleteUserScreenAuthorizations(@Param("userId") String userId);

    void insertUserScreenAuthorization(@Param("id") String id,
                                       @Param("userId") String userId,
                                       @Param("functionCode") String functionCode,
                                       @Param("createdBy") String createdBy,
                                       @Param("updatedBy") String updatedBy);

    int updateUserAccountPassword(@Param("userId") String userId,
                                  @Param("password") String password,
                                  @Param("updatedBy") String updatedBy);

    int updateUserAccountUpdatedBy(@Param("userId") String userId,
                                   @Param("updatedBy") String updatedBy);

    boolean existsAdminRoleAssignment(@Param("userId") String userId);

    List<Map<String, Object>> findApiFunctionCodes(@Param("httpMethod") String httpMethod);
}
