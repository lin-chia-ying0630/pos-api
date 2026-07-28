package com.alin.lin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder @Data @NoArgsConstructor @AllArgsConstructor
public class UserScreenAuthorizationRequest {
    /** 由 Admin 指定要維護畫面授權的使用者 ID。 */
    @NotBlank(message = "使用者 ID 不可空白") private String userId;
    @NotEmpty(message = "至少選擇一個畫面") private List<String> functionCodes;
}
