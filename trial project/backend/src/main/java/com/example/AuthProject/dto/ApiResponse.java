package com.example.AuthProject.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int status;
    private String message;
    private Map<String, String> errors;
    private T data;

    public static <T> ApiResponse<T> success(HttpStatus httpStatus, String message) {
        return ApiResponse.<T>builder()
                .status(httpStatus.value())
                .message(message)
                .errors(Collections.emptyMap())
                .build();
    }

    public static <T> ApiResponse<T> success(HttpStatus httpStatus, String message, T data) {
        return ApiResponse.<T>builder()
                .status(httpStatus.value())
                .message(message)
                .errors(Collections.emptyMap())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> failure(HttpStatus httpStatus, String message, Map<String, String> errors) {
        return ApiResponse.<T>builder()
                .status(httpStatus.value())
                .message(message)
                .errors(errors != null ? errors : Collections.emptyMap())
                .build();
    }
}
