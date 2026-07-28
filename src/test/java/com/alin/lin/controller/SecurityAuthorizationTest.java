package com.alin.lin.controller;

import com.alin.lin.config.PosCorsProperties;
import com.alin.lin.config.PosSecurityProperties;
import com.alin.lin.config.PasswordEncodingConfig;
import com.alin.lin.config.SecurityConfig;
import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.dao.UserAccountSecurityDao;
import com.alin.lin.dao.UserRoleAuthorizationDao;
import com.alin.lin.dto.CreateChangeCaseDto;
import com.alin.lin.dto.UpdateChangeCaseStatusDto;
import com.alin.lin.service.PolicyChangeService;
import com.alin.lin.service.UserAccountSecurityService;
import com.alin.lin.service.UserRoleAuthorizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PolicyChangeController.class, AuthController.class})
@Import({SecurityConfig.class, PasswordEncodingConfig.class})
@EnableConfigurationProperties({PosSecurityProperties.class, PosCorsProperties.class})
@TestPropertySource(properties = {
        "spring.profiles.active=test",
        "pos.security.enabled=true",
        "pos.security.require-https=false",
        "pos.security.maker-username=maker",
        "pos.security.maker-password=maker-secret",
        "pos.security.reviewer-username=reviewer",
        "pos.security.reviewer-password=reviewer-secret",
        "pos.cors.allowed-origins[0]=http://localhost:5173"
})
class SecurityAuthorizationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PolicyChangeService policyChangeService;

    @MockitoBean
    private PolicyChangeDao policyChangeDao;

    @MockitoBean
    private UserRoleAuthorizationService userRoleAuthorizationService;

    @MockitoBean
    private UserAccountSecurityService userAccountSecurityService;

    @MockitoBean
    private UserAccountSecurityDao userAccountSecurityDao;

    @MockitoBean
    private UserRoleAuthorizationDao userRoleAuthorizationDao;

    @BeforeEach
    void allowConfiguredTestScreens() {
        // 測試授權快照：只模擬已登入者持有的畫面權限；正式清單由
        // code_definition + user_screen_authorization 依 user_id 查詢，不以此測試資料為來源。
        given(userRoleAuthorizationService.findFunctionCodes(any())).willReturn(java.util.List.of(
                "MPS00001", "MPS00002", "MPS00003", "MPM00001", "MPM00002", "MPM00003",
                "MPM00004", "MPM00005", "MPM00006",
                "MCM00001", "MCM00002", "MUS00001"));
        // API 對應已由 code table 提供；此 slice test 只模擬建立保全案件所需的功能代碼。
        given(userRoleAuthorizationService.findApiFunctionCodes(any(), any()))
                .willReturn(java.util.List.of("MPS00001"));
    }

    @Test
    void invalidBasicCredentialsReturnJsonWithoutBrowserChallenge() throws Exception {
        mockMvc.perform(get("/api/auth/me").with(httpBasic("admin", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("尚未登入或帳號密碼錯誤"));
    }

    @Test
    void returnsResponseBodyDtoWhenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(get("/api/policies/P000000001/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Security-Policy",
                        "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("尚未登入或帳號密碼錯誤"));
    }

    @Test
    void makerCanCreateChangeCase() throws Exception {
        given(policyChangeService.createChangeCase(any())).willReturn(CreateChangeCaseDto.builder()
                .policyNo("P000000001")
                .policySeq(1)
                .changeCaseNo("C1150712001")
                .acceptanceStatus("P")
                .changeItemCodes(java.util.List.of("001", "002"))
                .build());

        mockMvc.perform(post("/api/change-cases")
                        .with(httpBasic("maker", "maker-secret"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "policyNo": "P000000001",
                                  "policySeq": 1,
                                  "changeItemCodes": ["001", "002"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.changeCaseNo").value("C1150712001"));
    }

    @Test
    void makerCanCreateChangeCaseWithMoreThanThreeItems() throws Exception {
        given(policyChangeService.createChangeCase(any())).willReturn(CreateChangeCaseDto.builder()
                .policyNo("P000000001")
                .policySeq(1)
                .changeCaseNo("C1150712002")
                .acceptanceStatus("P")
                .changeItemCodes(java.util.List.of("001", "002", "003", "004", "005", "006"))
                .build());

        mockMvc.perform(post("/api/change-cases")
                        .with(httpBasic("maker", "maker-secret"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "policyNo": "P000000001",
                                  "policySeq": 1,
                                  "changeItemCodes": ["001", "002", "003", "004", "005", "006"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.changeItemCodes.length()").value(6));
    }

    @Test
    void rejectsApiWhenRoleExistsButScreenWasNotAssigned() throws Exception {
        given(userRoleAuthorizationService.findFunctionCodes("maker")).willReturn(java.util.List.of("MPM00001"));

        mockMvc.perform(post("/api/change-cases")
                        .with(httpBasic("maker", "maker-secret"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"policyNo":"P000000001","policySeq":1,"changeItemCodes":["001"]}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessage").value("尚未授權功能代碼：MPS00001"));
    }

    @Test
    void reviewerCannotCreateChangeCase() throws Exception {
        mockMvc.perform(post("/api/change-cases")
                        .with(httpBasic("reviewer", "reviewer-secret"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("沒有執行此作業的權限"));
    }

    @Test
    void rejectsFabricatedChangeCaseNumber() throws Exception {
        mockMvc.perform(post("/api/change-cases/CUSTOM/address-change")
                        .with(httpBasic("maker", "maker-secret"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "policyNo": "P000000001",
                                  "policySeq": 1,
                                  "addressTypeCode": "01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("changeCaseNo 格式錯誤"));
    }

    @Test
    void rejectsNegativeInsuredAmount() throws Exception {
        mockMvc.perform(post("/api/change-cases/C1150718001/main-amount-change")
                        .with(httpBasic("maker", "maker-secret"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "policyNo": "P000000001",
                                  "policySeq": 1,
                                  "insuredAmount": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("insuredAmount 不可小於 0"));
    }

    @Test
    void makerCannotReviewChangeCase() throws Exception {
        mockMvc.perform(patch("/api/change-cases/C1150712001/status")
                        .with(httpBasic("maker", "maker-secret"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("沒有執行此作業的權限"));
    }

    @Test
    void reviewerCanCompleteChangeCase() throws Exception {
        given(policyChangeService.updateChangeCaseStatus(any(), any())).willReturn(UpdateChangeCaseStatusDto.builder()
                .policyNo("P000000001")
                .policySeq(1)
                .changeCaseNo("C1150712001")
                .acceptanceStatus("S")
                .appliedItemCount(1)
                .build());

        mockMvc.perform(patch("/api/change-cases/C1150712001/status")
                        .with(httpBasic("reviewer", "reviewer-secret"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "policyNo": "P000000001",
                                  "policySeq": 1,
                                  "acceptanceStatus": "S"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.acceptanceStatus").value("S"));
    }

    @Test
    void returnsAllAuthenticatedReviewerRoles() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .with(httpBasic("reviewer", "reviewer-secret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("reviewer"))
                .andExpect(jsonPath("$.data.roles", containsInAnyOrder("REVIEWER", "ADMIN")))
                .andExpect(jsonPath("$.data.securityEnabled").value(true));
    }

    @Test
    void returnsMakerAndUserRolesForMakerAccount() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .with(httpBasic("maker", "maker-secret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("maker"))
                .andExpect(jsonPath("$.data.roles", containsInAnyOrder("MAKER", "USER")));
    }

    @Test
    void allowsConfiguredCorsOrigin() throws Exception {
        mockMvc.perform(options("/api/policies/P000000001/1")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
