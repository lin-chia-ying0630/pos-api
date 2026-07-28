package com.alin.lin.config;

import com.alin.lin.service.UserRoleAuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScreenAuthorizationFilterTest {
    private final UserRoleAuthorizationService authorizationService = mock(UserRoleAuthorizationService.class);
    private final FilterChain chain = mock(FilterChain.class);
    private final ScreenAuthorizationFilter filter =
            new ScreenAuthorizationFilter(authorizationService, new ObjectMapper());

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void makerFunctionCanReadSharedPolicyDetail() throws Exception {
        authenticate("maker");
        when(authorizationService.findFunctionCodes("maker")).thenReturn(List.of("MPS00001"));
        when(authorizationService.findApiFunctionCodes("GET", "/api/policies/P000000001/1"))
                .thenReturn(List.of("MPS00001", "MPM00001"));

        MockHttpServletResponse response = perform("GET", "/api/policies/P000000001/1");

        assertThat(response.getStatus()).isEqualTo(200);
        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void queryAndReviewFunctionsCanReadChangeCaseDetail() throws Exception {
        authenticate("reviewer");
        when(authorizationService.findFunctionCodes("reviewer")).thenReturn(List.of("MPS00003"));
        when(authorizationService.findApiFunctionCodes(
                "GET", "/api/policies/P000000001/1/change-cases/C20260724001"))
                .thenReturn(List.of("MPS00002", "MPS00003"));

        MockHttpServletResponse response =
                perform("GET", "/api/policies/P000000001/1/change-cases/C20260724001");

        assertThat(response.getStatus()).isEqualTo(200);
        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void postalLookupRequiresCreateChangeFunction() throws Exception {
        authenticate("user");
        when(authorizationService.findFunctionCodes("user")).thenReturn(List.of("MPM00001"));
        when(authorizationService.findApiFunctionCodes("GET", "/api/postal-codes/100001"))
                .thenReturn(List.of("MPS00001"));

        MockHttpServletResponse response = perform("GET", "/api/postal-codes/100001");

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("MPS00001");
        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void metadataUsesItsOwnPolicyFunction() throws Exception {
        authenticate("maker");
        when(authorizationService.findFunctionCodes("maker")).thenReturn(List.of("MPM00002"));
        when(authorizationService.findApiFunctionCodes("GET", "/api/policy-ui-metadata/address"))
                .thenReturn(List.of("MPM00002", "MPM00005"));

        MockHttpServletResponse response = perform("GET", "/api/policy-ui-metadata/address");

        assertThat(response.getStatus()).isEqualTo(200);
        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void policyQueryPermissionCannotModifyPolicyMaster() throws Exception {
        authenticate("query-user");
        when(authorizationService.findFunctionCodes("query-user")).thenReturn(List.of("MPM00001"));
        when(authorizationService.findApiFunctionCodes("PUT", "/api/policy-masters"))
                .thenReturn(List.of("MPM00004"));

        MockHttpServletResponse response = perform("PUT", "/api/policy-masters");

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("MPM00004");
        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void eachPolicyMaintenancePermissionOnlyModifiesItsOwnEntity() throws Exception {
        authenticate("maker");
        when(authorizationService.findFunctionCodes("maker")).thenReturn(List.of("MPM00004"));
        when(authorizationService.findApiFunctionCodes("PUT", "/api/policy-masters"))
                .thenReturn(List.of("MPM00004"));
        when(authorizationService.findApiFunctionCodes("PUT", "/api/policy-details/addresses"))
                .thenReturn(List.of("MPM00005"));
        when(authorizationService.findApiFunctionCodes("PUT", "/api/policy-details/rides"))
                .thenReturn(List.of("MPM00006"));

        assertThat(perform("PUT", "/api/policy-masters").getStatus()).isEqualTo(200);
        assertThat(perform("PUT", "/api/policy-details/addresses").getStatus()).isEqualTo(403);
        assertThat(perform("PUT", "/api/policy-details/rides").getStatus()).isEqualTo(403);

        when(authorizationService.findFunctionCodes("maker")).thenReturn(List.of("MPM00005"));
        assertThat(perform("PUT", "/api/policy-details/addresses").getStatus()).isEqualTo(200);

        when(authorizationService.findFunctionCodes("maker")).thenReturn(List.of("MPM00006"));
        assertThat(perform("PUT", "/api/policy-details/rides").getStatus()).isEqualTo(200);
    }

    private MockHttpServletResponse perform(String method, String path) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRequestURI(path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    private void authenticate(String userId) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(userId, "N/A", List.of()));
    }
}
