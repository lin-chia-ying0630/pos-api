package com.alin.lin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pos.security")
public class PosSecurityProperties {
    // 預設開啟角色驗證；只有 local profile 可明確關閉。
    private boolean enabled;

    // 正式環境要求請求經 HTTPS 或可信任反向代理傳入。
    private boolean requireHttps;

    // 經辦人員帳號與密碼。
    private String makerUsername;
    private String makerPassword;

    // 覆核人員帳號與密碼。
    private String reviewerUsername;
    private String reviewerPassword;

    // 使用者授權維護帳號與密碼。
    private String userUsername;
    private String userPassword;

    // 使用者授權覆核帳號與密碼。
    private String adminUsername;
    private String adminPassword;
}
