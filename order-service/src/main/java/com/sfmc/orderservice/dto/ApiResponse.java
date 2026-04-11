package com.sfmc.orderservice.dto;

import java.time.LocalDateTime;

/**
 * Enveloppe standard pour toutes les reponses API
 * {
 *   "success": true,
 *   "message": "Commande creee avec succes",
 *   "data": { ... },
 *   "timestamp": "2026-04-11T02:30:00"
 * }
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
}