package com.alin.lin.config;

import com.alin.lin.dto.ResponseBodyDto;
import com.alin.lin.service.UserRoleAuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** 後端依 user_id 再驗證功能代碼，避免只隱藏前端選單卻仍可直接呼叫 API。 */
public class ScreenAuthorizationFilter extends OncePerRequestFilter {
    private final UserRoleAuthorizationService authorizationService;
    private final ObjectMapper objectMapper;

    public ScreenAuthorizationFilter(UserRoleAuthorizationService authorizationService, ObjectMapper objectMapper) {
        this.authorizationService = authorizationService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        List<String> requiredCodes = resolveFunctionCodes(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (requiredCodes.isEmpty() || authentication == null || !authentication.isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }
        List<String> functionCodes = authorizationService.findFunctionCodes(authentication.getName());
        if (requiredCodes.stream().noneMatch(functionCodes::contains)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), ResponseBodyDto.builder()
                    .success(false).errorMessage("尚未授權功能代碼：" + String.join(" / ", requiredCodes)).build());
            return;
        }
        chain.doFilter(request, response);
    }

    private List<String> resolveFunctionCodes(HttpServletRequest request) {
        if ("OPTIONS".equals(request.getMethod())) return List.of();
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/") || path.startsWith("/actuator/") || path.equals("/error")) return List.of();
        List<String> configuredCodes =
                authorizationService.findApiFunctionCodes(request.getMethod(), path);
        if (!configuredCodes.isEmpty()) return configuredCodes;
        return List.of();
    }
}
