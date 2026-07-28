package com.alin.lin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder @Data @NoArgsConstructor @AllArgsConstructor
public class UserScreenAuthorizationDto {
    /** 使用者 ID，也是畫面授權的直接歸屬鍵。 */
    private String userId;
    private List<String> functionCodes;
    private String reviewStatus;
    /** 該使用者所有畫面授權中最早的建立資訊。 */
    private String createdBy;
    private LocalDateTime createdAt;
    /** 該使用者所有畫面授權中最新的更新資訊。 */
    private String updatedBy;
    private LocalDateTime updatedAt;
}
