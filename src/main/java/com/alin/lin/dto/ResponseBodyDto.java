package com.alin.lin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBodyDto<T> {
    // 是否成功
    private boolean success;

    // 回覆訊息
    @Builder.Default
    private String message = "";

    // 訊息代碼
    @Builder.Default
    private String messageCode = "";

    // 錯誤訊息
    @Builder.Default
    private String errorMessage = "";

    // 回覆資料
    private T data;
}
