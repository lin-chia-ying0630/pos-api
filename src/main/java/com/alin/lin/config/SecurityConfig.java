package com.alin.lin.config;

import com.alin.lin.dto.ResponseBodyDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import com.alin.lin.service.UserAccountSecurityService;
import com.alin.lin.service.UserRoleAuthorizationService;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final PosCorsProperties corsProperties;
    private final PosSecurityProperties securityProperties;
    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final UserRoleAuthorizationService authorizationService;
    private final PasswordEncoder passwordEncoder;

    // 只有 local/prod profile 的 jdbcUserDetailsService bean 才需要此依賴；
    // 以 @Autowired(required = false) 注入，避免 @WebMvcTest 的 sliced context
    // 因找不到此 bean 而啟動失敗。
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private UserAccountSecurityService userAccountSecurityService;

    public SecurityConfig(
            PosCorsProperties corsProperties,
            PosSecurityProperties securityProperties,
            ObjectMapper objectMapper,
            Environment environment,
            UserRoleAuthorizationService authorizationService,
            PasswordEncoder passwordEncoder
    ) {
        this.corsProperties = corsProperties;
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
        this.environment = environment;
        this.authorizationService = authorizationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // API 與同源 SPA 都採明確 Authorization header，不使用 Cookie 型登入；
                // 因此停用 CSRF，但仍以 CSP、防 clickjacking 與 referrer policy 降低瀏覽器端攻擊面。
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"))
                        .frameOptions(frame -> frame.deny())
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .addHeaderWriter(new StaticHeadersWriter(
                                "Permissions-Policy", "camera=(), geolocation=(), microphone=()"))
                );

        if (!securityProperties.isEnabled()) {
            if (!environment.acceptsProfiles(Profiles.of("local", "test"))) {
                throw new IllegalStateException("POS Security 只能在 local 或 test profile 關閉");
            }
            return http
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }

        if (securityProperties.isRequireHttps()) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        return http
                // BasicAuthenticationFilter 驗證失敗時也必須使用 JSON entry point。
                // 若沿用框架預設值會回傳 WWW-Authenticate，瀏覽器便跳出原生登入視窗，
                // 遮住 Vue 自有登入頁並造成使用者反覆輸入仍無法進入。
                .httpBasic(basic -> basic.authenticationEntryPoint((request, response, exception) ->
                        writeSecurityError(response, HttpServletResponse.SC_UNAUTHORIZED, "尚未登入或帳號密碼錯誤")))
                .addFilterAfter(new ScreenAuthorizationFilter(authorizationService, objectMapper), BasicAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeSecurityError(response, HttpServletResponse.SC_UNAUTHORIZED, "尚未登入或帳號密碼錯誤"))
                        .accessDeniedHandler((request, response, exception) ->
                                writeSecurityError(response, HttpServletResponse.SC_FORBIDDEN, "沒有執行此作業的權限"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/change-cases/*/status").hasRole("REVIEWER")
                        .requestMatchers(HttpMethod.PATCH, "/api/change-reviews/*/decision").hasRole("REVIEWER")
                        .requestMatchers(HttpMethod.POST, "/api/change-cases", "/api/change-cases/**").hasRole("MAKER")
                        .requestMatchers(HttpMethod.POST, "/api/policy-masters").hasAnyRole("MAKER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/policy-masters").hasAnyRole("MAKER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/policy-masters/**").hasAnyRole("MAKER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/policy-details/**").hasAnyRole("MAKER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/policy-details/**").hasAnyRole("MAKER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/policy-details/**").hasAnyRole("MAKER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/user-authorizations/codes").hasAnyRole("MAKER", "REVIEWER")
                        .requestMatchers(HttpMethod.POST, "/api/user-authorizations/codes").hasAnyRole("MAKER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/user-authorizations/codes").hasAnyRole("MAKER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/user-authorizations/codes/**").hasAnyRole("MAKER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/user-authorizations/codes/*/*/*/review").hasRole("REVIEWER")
                        .requestMatchers(HttpMethod.POST, "/api/user-authorizations/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/user-authorizations/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/user-authorizations/users/*/password").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/user-authorizations/users/roles").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/user-authorizations/users/roles").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/user-authorizations/users/screens").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("MAKER", "REVIEWER", "USER", "ADMIN")
                        .anyRequest().denyAll()
                )
                .build();
    }

    @Bean
    @Profile("test")
    @ConditionalOnProperty(prefix = "pos.security", name = "enabled", havingValue = "true")
    public UserDetailsService userDetailsService() {
        requireCredentialPair(
                securityProperties.getMakerUsername(),
                securityProperties.getMakerPassword(),
                "POS_MAKER_USERNAME",
                "POS_MAKER_PASSWORD"
        );
        requireCredentialPair(
                securityProperties.getReviewerUsername(),
                securityProperties.getReviewerPassword(),
                "POS_REVIEWER_USERNAME",
                "POS_REVIEWER_PASSWORD"
        );
        requireOptionalCredentialPair(securityProperties.getUserUsername(), securityProperties.getUserPassword(), "POS_USER_USERNAME", "POS_USER_PASSWORD");
        requireOptionalCredentialPair(securityProperties.getAdminUsername(), securityProperties.getAdminPassword(), "POS_ADMIN_USERNAME", "POS_ADMIN_PASSWORD");
        List<UserDetails> users = configuredIdentities().values().stream()
                .map(identity -> User.withUsername(identity.username())
                        .password(passwordEncoder.encode(identity.password()))
                        .roles(identity.roles().toArray(String[]::new))
                        .build())
                .toList();
        return new InMemoryUserDetailsManager(users);
    }

    @Bean
    @Profile({"local", "prod"})
    @ConditionalOnProperty(prefix = "pos.security", name = "enabled", havingValue = "true")
    public UserDetailsService jdbcUserDetailsService() {
        configuredIdentities().values().forEach(identity -> {
            provisionUser(identity);
        });
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                return userAccountSecurityService.loadUserByUsername(username);
            }
        };
    }

    @Bean
    @Profile({"local", "test"})
    @ConditionalOnProperty(prefix = "pos.security", name = "enabled", havingValue = "false")
    public UserDetailsService localUserDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    private void provisionUser(ConfiguredIdentity identity) {
        Set<String> authorities = new LinkedHashSet<>();
        identity.roles().forEach(role -> authorities.add("ROLE_" + role));
        // 設定層只組合環境參數；帳號查詢、寫入與角色重建由 Service/DAO 三層流程負責。
        userAccountSecurityService.synchronizeConfiguredUser(
                identity.username(),
                passwordEncoder.encode(identity.password()),
                authorities
        );
    }

    private Map<String, ConfiguredIdentity> configuredIdentities() {
        Map<String, ConfiguredIdentity> identities = new LinkedHashMap<>();
        // local/prod 可直接使用資料庫既有帳號；只有明確提供環境帳密時才同步固定角色組合。
        addOptionalConfiguredRole(identities, securityProperties.getMakerUsername(), securityProperties.getMakerPassword(), "MAKER");
        addOptionalConfiguredRole(identities, securityProperties.getMakerUsername(), securityProperties.getMakerPassword(), "USER");
        addOptionalConfiguredRole(identities, securityProperties.getReviewerUsername(), securityProperties.getReviewerPassword(), "REVIEWER");
        addOptionalConfiguredRole(identities, securityProperties.getReviewerUsername(), securityProperties.getReviewerPassword(), "ADMIN");
        addOptionalConfiguredRole(identities, securityProperties.getUserUsername(), securityProperties.getUserPassword(), "USER");
        addOptionalConfiguredRole(identities, securityProperties.getAdminUsername(), securityProperties.getAdminPassword(), "ADMIN");
        return identities;
    }

    private void addOptionalConfiguredRole(Map<String, ConfiguredIdentity> identities, String username, String password, String role) {
        if ((username == null || username.isBlank()) && (password == null || password.isBlank())) return;
        addConfiguredRole(identities, username, password, role);
    }

    private void addConfiguredRole(Map<String, ConfiguredIdentity> identities, String username, String password, String role) {
        requireCredentialPair(username, password, "POS_" + role + "_USERNAME", "POS_" + role + "_PASSWORD");
        ConfiguredIdentity existing = identities.get(username);
        if (existing != null && !existing.password().equals(password)) {
            throw new IllegalStateException("同一使用者 " + username + " 的多個角色必須使用相同密碼");
        }
        if (existing == null) {
            identities.put(username, new ConfiguredIdentity(username, password, new LinkedHashSet<>(Set.of(role))));
        } else {
            existing.roles().add(role);
        }
    }

    private void requireOptionalCredentialPair(String username, String password, String usernameEnvironment, String passwordEnvironment) {
        if ((username == null || username.isBlank()) && (password == null || password.isBlank())) return;
        requireCredentialPair(username, password, usernameEnvironment, passwordEnvironment);
    }

    private record ConfiguredIdentity(String username, String password, Set<String> roles) {
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void requireCredentialPair(String username, String password, String usernameEnvironment, String passwordEnvironment) {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("啟用 POS Security 時必須設定 " + usernameEnvironment);
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("啟用 POS Security 時必須設定 " + passwordEnvironment);
        }
        if (password.length() < 12) {
            throw new IllegalStateException(passwordEnvironment + " 至少需要 12 個字元");
        }
        if (username.equals(password)) {
            throw new IllegalStateException(passwordEnvironment + " 不可與帳號相同");
        }
    }

    private void writeSecurityError(HttpServletResponse response, int status, String errorMessage) throws java.io.IOException {
        response.setStatus(status);
        response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ResponseBodyDto.builder()
                .success(false)
                .message("")
                .messageCode("")
                .errorMessage(errorMessage)
                .data(null)
                .build());
    }
}
