package com.alin.lin.controller;

import com.alin.lin.config.PosSecurityProperties;
import com.alin.lin.dto.CurrentUserDto;
import com.alin.lin.dto.ResponseBodyDto;
import com.alin.lin.util.ResponseUtil;
import com.alin.lin.service.UserRoleAuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final PosSecurityProperties securityProperties;
    private final UserRoleAuthorizationService authorizationService;

    public AuthController(PosSecurityProperties securityProperties, UserRoleAuthorizationService authorizationService) {
        this.securityProperties = securityProperties;
        this.authorizationService = authorizationService;
    }

    // 畫面對應：登入頁驗證 userId，並回傳該使用者全部角色；同一人可同時擁有兩個以上角色。
    @GetMapping("/me")
    public ResponseEntity<ResponseBodyDto<CurrentUserDto>> currentUser(Authentication authentication) {
        if (!securityProperties.isEnabled()) {
            return ResponseUtil.ok(CurrentUserDto.builder()
                    .userId("local-development")
                    .roles(List.of("MAKER", "REVIEWER"))
                    .functionCodes(authorizationService.findAvailableFunctionCodes())
                    .securityEnabled(false)
                    .build());
        }
        List<String> roles = authentication.getAuthorities().stream()
                .filter(authority -> authority.getAuthority().startsWith("ROLE_"))
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .distinct()
                .sorted()
                .toList();
        return ResponseUtil.ok(CurrentUserDto.builder()
                .userId(authentication.getName())
                .roles(roles)
                .functionCodes(authorizationService.findFunctionCodes(authentication.getName()))
                .securityEnabled(true)
                .build());
    }
}
