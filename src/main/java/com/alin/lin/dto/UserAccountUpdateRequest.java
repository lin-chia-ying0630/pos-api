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
public class UserAccountUpdateRequest {
    @NotBlank(message = "使用者 ID 不可空白")
    @Size(max = 128, message = "使用者 ID 不可超過 128 個字元")
    private String userId;
    private boolean enabled;
    @NotEmpty(message = "至少選擇一個角色")
    private List<String> roles;
}
