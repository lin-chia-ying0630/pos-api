package com.alin.lin.util;

import com.alin.lin.dto.ResponseBodyDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 程式說明:ResponseUtil
 *
 * @param :
 * @author :
 * @date:
 * @return :
 **/
public class ResponseUtil {

    public static <T> ResponseEntity<ResponseBodyDto<T>> ok() {
        return builder(HttpStatus.OK, true, null, "執行成功", " ", " ");
    }

    public static <T> ResponseEntity<ResponseBodyDto<T>> ok(String message, String messageCode) {
        return builder(HttpStatus.OK, true, null, message, messageCode, " ");
    }

    public static <T> ResponseEntity<ResponseBodyDto<T>> ok(String message, T data) {
        return builder(HttpStatus.OK, true, data, message, "", " ");
    }

    public static <T> ResponseEntity<ResponseBodyDto<T>> ok(String message, String messageCode, T data) {
        return builder(HttpStatus.OK, true, data, message, messageCode, " ");
    }

    public static <T> ResponseEntity<ResponseBodyDto<T>> ok(T data) {
        return builder(HttpStatus.OK, true, data, "執行成功", "", " ");
    }

    public static <T> ResponseEntity<ResponseBodyDto<T>> ok(String massage) {
        return builder(HttpStatus.OK, true, null, massage, "", "");
    }

    public static <T> ResponseEntity<ResponseBodyDto<T>> ok(T data, String message) {
        return builder(HttpStatus.OK, true, data, message, "", " ");
    }

    public static <T> ResponseEntity<ResponseBodyDto<T>> created(T data) {
        return builder(HttpStatus.CREATED, true, data, "執行成功", "", " ");
    }

    public static <T> ResponseEntity<ResponseBodyDto<T>> noContent(String message) {
        return builder(HttpStatus.OK, true, null, message, "", " ");
    }

    public static <T> ResponseEntity<ResponseBodyDto<T>> badRequest(String errorMessage) {
        return builder(HttpStatus.BAD_REQUEST, false, null, "", "", errorMessage);
    }

    public static <T> ResponseEntity<ResponseBodyDto<T>> notFound(String errorMessage) {
        return builder(HttpStatus.NOT_FOUND, false, null, "", "", errorMessage);
    }

    public static <T> ResponseEntity<ResponseBodyDto<T>> conflict(String errorMessage) {
        return builder(HttpStatus.CONFLICT, false, null, "", "", errorMessage);
    }

    public static <T> ResponseEntity<ResponseBodyDto<T>> forbidden(String errorMessage) {
        return builder(HttpStatus.FORBIDDEN, false, null, "", "", errorMessage);
    }

    public static <T> ResponseEntity<ResponseBodyDto<T>> serverError(String errorMessage) {
        return builder(HttpStatus.INTERNAL_SERVER_ERROR, false, null, "", "", errorMessage);
    }

    public static <T> ResponseEntity<ResponseBodyDto<T>> serviceUnavailable(String errorMessage) {
        return builder(HttpStatus.SERVICE_UNAVAILABLE, false, null, "", "", errorMessage);
    }


    public static <T> ResponseEntity<ResponseBodyDto<T>> builder(HttpStatus httpStatus, boolean isSuccess, T data, String message, String messageCode, String errorMessage) {

        return ResponseEntity.status(httpStatus).body(
                ResponseBodyDto.<T>builder()
                        .success(isSuccess)
                        .message(message)
                        .messageCode(messageCode)
                        .errorMessage(errorMessage)
                        .data(data)
                        .build()
        );
    }

}
