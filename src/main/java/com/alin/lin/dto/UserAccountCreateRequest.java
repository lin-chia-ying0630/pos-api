package com.alin.lin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountCreateRequest {
    @NotBlank(message = "使用者 ID 不可空白")
    @Size(max = 128, message = "使用者 ID 不可超過 128 個字元")
    private String userId;

    @NotBlank(message = "初始密碼不可空白")
    @Size(min = 12, max = 128, message = "初始密碼長度須為 12 至 128 個字元")
    private String password;

    private boolean enabled;

    @NotEmpty(message = "至少選擇一個角色")
    private List<String> roles;
}
