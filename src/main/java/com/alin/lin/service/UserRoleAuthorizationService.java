package com.alin.lin.service;

import com.alin.lin.dto.UserRoleAuthorizationDto;
import com.alin.lin.dto.UserRoleAuthorizationRequest;
import com.alin.lin.dto.UserAccountCreateRequest;
import com.alin.lin.dto.UserAccountUpdateRequest;
import com.alin.lin.dto.UserPasswordResetRequest;
import com.alin.lin.dto.UserScreenAuthorizationDto;
import com.alin.lin.dto.UserScreenAuthorizationRequest;

import java.util.List;

public interface UserRoleAuthorizationService {
    List<UserRoleAuthorizationDto> findAll();
    UserRoleAuthorizationDto createUser(UserAccountCreateRequest request, String operatorId);
    UserRoleAuthorizationDto updateUser(UserAccountUpdateRequest request, String operatorId);
    void resetPassword(String userId, UserPasswordResetRequest request, String operatorId);
    UserRoleAuthorizationDto addRoles(UserRoleAuthorizationRequest request, String operatorId);
    UserRoleAuthorizationDto replaceRoles(UserRoleAuthorizationRequest request, String operatorId);
    List<UserScreenAuthorizationDto> findAllScreenAuthorizations();
    List<String> findFunctionCodes(String userId);
    List<String> findAvailableFunctionCodes();
    List<String> findApiFunctionCodes(String httpMethod, String requestPath);
    UserScreenAuthorizationDto replaceScreens(UserScreenAuthorizationRequest request, String operatorId);
}
