package com.leander.lottery.admin.exception;

/**
 * 找不到對應的物件時拋出此異常
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}