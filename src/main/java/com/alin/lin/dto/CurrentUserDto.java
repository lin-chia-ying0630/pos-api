package com.alin.lin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDto {
    // 登入帳號。
    /** 登入帳號的穩定識別鍵；對應 user_account.user_id。 */
    private String userId;

    // 使用者全部角色，例如同時具有 MAKER、REVIEWER 與 ADMIN；不得只回傳單一角色。
    private List<String> roles;

    // 使用者實際可進入的功能代碼，由資料庫畫面授權提供。
    private List<String> functionCodes;

    // 是否啟用正式權限驗證。
    private boolean securityEnabled;
}
