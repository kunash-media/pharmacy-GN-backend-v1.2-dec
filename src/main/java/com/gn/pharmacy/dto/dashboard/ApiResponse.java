package com.gn.pharmacy.dto.dashboard;


public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        int status
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "Success", 200);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, 200);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, 500);
    }

    public static <T> ApiResponse<T> error(String message, int status) {
        return new ApiResponse<>(false, null, message, status);
    }
}