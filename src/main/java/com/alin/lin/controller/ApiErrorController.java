package com.alin.lin.controller;

import com.alin.lin.dto.ResponseBodyDto;
import com.alin.lin.util.ResponseUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiErrorController implements ErrorController {
    @RequestMapping("/error")
    public ResponseEntity<ResponseBodyDto<Void>> handleError(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (statusCode instanceof Integer code) {
            status = HttpStatus.resolve(code) == null ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(code);
        }

        String errorMessage = switch (status) {
            case FORBIDDEN -> "無權限";
            case UNAUTHORIZED -> "尚未登入或登入資訊已失效";
            case NOT_FOUND -> "找不到 API 路徑";
            case METHOD_NOT_ALLOWED -> "HTTP 方法不允許";
            default -> "系統發生未預期錯誤";
        };
        return ResponseUtil.builder(status, false, null, "", "", errorMessage);
    }
}
