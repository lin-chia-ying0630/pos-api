package com.alin.lin.controller;

import com.alin.lin.entity.CodeDescription;
import com.alin.lin.dto.CodeDescriptionCreateRequest;
import com.alin.lin.dto.ResponseBodyDto;
import com.alin.lin.dto.UserRoleAuthorizationDto;
import com.alin.lin.dto.UserRoleAuthorizationRequest;
import com.alin.lin.dto.UserAccountCreateRequest;
import com.alin.lin.dto.UserAccountUpdateRequest;
import com.alin.lin.dto.UserPasswordResetRequest;
import com.alin.lin.dto.UserScreenAuthorizationDto;
import com.alin.lin.dto.UserScreenAuthorizationRequest;
import com.alin.lin.service.CodeDescriptionService;
import com.alin.lin.service.UserRoleAuthorizationService;
import com.alin.lin.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;

@RestController
@RequestMapping("/api/user-authorizations")
public class UserAuthorizationController {
    private final CodeDescriptionService codeDescriptionService;
    private final UserRoleAuthorizationService userRoleAuthorizationService;

    public UserAuthorizationController(CodeDescriptionService codeDescriptionService,
                                       UserRoleAuthorizationService userRoleAuthorizationService) {
        this.codeDescriptionService = codeDescriptionService;
        this.userRoleAuthorizationService = userRoleAuthorizationService;
    }

    // 畫面對應：使用者授權頁顯示新增、修改、刪除、覆核與角色對照。
    @GetMapping
    public ResponseEntity<ResponseBodyDto<List<CodeDescription>>> findPermissions() {
        return ResponseUtil.ok(codeDescriptionService.findUserAuthorizationPermissions());
    }

    // USER 可查詢所有帳號的角色集合；前端只讀顯示，不提供寫入操作。
    @GetMapping("/users")
    public ResponseEntity<ResponseBodyDto<List<UserRoleAuthorizationDto>>> findUsers() {
        return ResponseUtil.ok(userRoleAuthorizationService.findAll());
    }

    // 只有 ADMIN 可建立新登入帳號並一次設定啟用狀態與初始角色。
    @PostMapping("/users")
    public ResponseEntity<ResponseBodyDto<UserRoleAuthorizationDto>> createUser(
            @Valid @RequestBody UserAccountCreateRequest request, Authentication authentication) {
        return ResponseUtil.created(userRoleAuthorizationService.createUser(request, authentication.getName()));
    }

    // 只有 ADMIN 可調整既有帳號的啟用狀態與完整角色集合，使用者 ID 不可修改。
    @PutMapping("/users")
    public ResponseEntity<ResponseBodyDto<UserRoleAuthorizationDto>> updateUser(
            @Valid @RequestBody UserAccountUpdateRequest request, Authentication authentication) {
        return ResponseUtil.ok(userRoleAuthorizationService.updateUser(request, authentication.getName()));
    }

    // 只有 ADMIN 可重設指定帳號密碼；密碼不回傳，也不寫入稽核內容。
    @PatchMapping("/users/{userId}/password")
    public ResponseEntity<ResponseBodyDto<Void>> resetPassword(
            @PathVariable String userId, @Valid @RequestBody UserPasswordResetRequest request,
            Authentication authentication) {
        userRoleAuthorizationService.resetPassword(userId, request, authentication.getName());
        return ResponseUtil.ok(null, "密碼已重設");
    }

    // 只有 ADMIN 可替既有帳號複選角色；畫面授權另行選擇，不由角色帶出。
    @PostMapping("/users/roles")
    public ResponseEntity<ResponseBodyDto<UserRoleAuthorizationDto>> addRoles(
            @Valid @RequestBody UserRoleAuthorizationRequest request, Authentication authentication) {
        return ResponseUtil.created(userRoleAuthorizationService.addRoles(request, authentication.getName()));
    }

    // 只有 ADMIN 可取代既有帳號的完整角色集合，立即生效並建立 S 狀態稽核。
    @PutMapping("/users/roles")
    public ResponseEntity<ResponseBodyDto<UserRoleAuthorizationDto>> replaceRoles(
            @Valid @RequestBody UserRoleAuthorizationRequest request, Authentication authentication) {
        return ResponseUtil.ok(userRoleAuthorizationService.replaceRoles(request, authentication.getName()));
    }

    @GetMapping("/users/screens")
    public ResponseEntity<ResponseBodyDto<List<UserScreenAuthorizationDto>>> findScreens() {
        return ResponseUtil.ok(userRoleAuthorizationService.findAllScreenAuthorizations());
    }

    @PutMapping("/users/screens")
    // 只有 ADMIN 可將後端功能代碼清單中的多個畫面直接掛到指定 userId。
    public ResponseEntity<ResponseBodyDto<UserScreenAuthorizationDto>> replaceScreens(
            @Valid @RequestBody UserScreenAuthorizationRequest request, Authentication authentication) {
        return ResponseUtil.ok(userRoleAuthorizationService.replaceScreens(request, authentication.getName()));
    }

    // 畫面對應：Code 清單入口，顯示所有代碼與中文說明。
    @GetMapping("/codes")
    public ResponseEntity<ResponseBodyDto<List<CodeDescription>>> findAllCodes() {
        return ResponseUtil.ok(codeDescriptionService.findAllCodes());
    }

    // 畫面對應：代碼對照表的新增資料，僅限 maker。
    @PostMapping("/codes")
    public ResponseEntity<ResponseBodyDto<CodeDescription>> createCode(@Valid @RequestBody CodeDescriptionCreateRequest request, Authentication authentication) {
        return ResponseUtil.created(codeDescriptionService.createCode(request, authentication.getName()));
    }

    // 畫面對應：代碼對照表修改與刪除支線，僅限 maker。
    @PutMapping("/codes")
    public ResponseEntity<ResponseBodyDto<CodeDescription>> updateCode(@Valid @RequestBody CodeDescriptionCreateRequest request, Authentication authentication) {
        return ResponseUtil.ok(codeDescriptionService.updateCode(request, authentication.getName()));
    }

    @DeleteMapping("/codes/{codeGroup}/{codeField}/{codeBefore}")
    public ResponseEntity<ResponseBodyDto<Void>> deleteCode(@PathVariable String codeGroup, @PathVariable String codeField,
                                                             @PathVariable String codeBefore, Authentication authentication) {
        codeDescriptionService.deleteCode(codeGroup, codeField, codeBefore, authentication.getName());
        return ResponseUtil.noContent("代碼已刪除");
    }

    // 畫面對應：代碼對照表覆核支線，僅限 reviewer。
    @PatchMapping("/codes/{codeGroup}/{codeField}/{codeBefore}/review")
    public ResponseEntity<ResponseBodyDto<CodeDescription>> reviewCode(@PathVariable String codeGroup,
                                                                          @PathVariable String codeField,
                                                                          @PathVariable String codeBefore,
                                                                          Authentication authentication) {
        return ResponseUtil.ok(codeDescriptionService.reviewCode(codeGroup, codeField, codeBefore,
                authentication.getName()));
    }
}
