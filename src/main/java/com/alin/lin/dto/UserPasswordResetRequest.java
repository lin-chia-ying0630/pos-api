package com.alin.lin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordResetRequest {
    @NotBlank(message = "新密碼不可空白")
    @Size(min = 12, max = 128, message = "新密碼長度須為 12 至 128 個字元")
    private String password;
}
