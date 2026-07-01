package com.smartmess.backend.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private LocalDateTime timestamp;

    private boolean success;

    private String message;

    private String path;

    private T data;

    /**
     * Creates a successful API response.
     */
    public static <T> ApiResponse<T> success(String message, String path, T data) {
        return new ApiResponse<T>(
                LocalDateTime.now(),
                true,
                message,
                path,
                data
        );
    }

    /**
     * Creates a failed API response.
     */
    public static <T> ApiResponse<T> failure(String message, String path, T data) {
        return new ApiResponse<T>(
                LocalDateTime.now(),
                false,
                message,
                path,
                data
        );
    }
}