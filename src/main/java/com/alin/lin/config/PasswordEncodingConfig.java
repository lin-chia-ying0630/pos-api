package com.alin.lin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 密碼雜湊的共用設定。
 *
 * <p>獨立於 {@link SecurityConfig}，避免業務授權 Service 注入 PasswordEncoder 時
 * 與 Web Security 設定形成循環依賴。</p>
 */
@Configuration
public class PasswordEncodingConfig {
    public static final String CURRENT_ENCODING_ID = "argon2@SpringSecurity_v5_8";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return createPasswordEncoder();
    }

    public static PasswordEncoder createPasswordEncoder() {
        Map<String, PasswordEncoder> encoders = new LinkedHashMap<>();
        encoders.put(CURRENT_ENCODING_ID, Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("bcrypt", new BCryptPasswordEncoder());

        DelegatingPasswordEncoder encoder = new DelegatingPasswordEncoder(CURRENT_ENCODING_ID, encoders);
        // 遷移前的資料沒有 {bcrypt} 識別前綴；僅在比對時以 BCrypt 相容，
        // 所有新建與重設密碼仍由 CURRENT_ENCODING_ID 寫成 Argon2id。
        encoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return encoder;
    }
}
