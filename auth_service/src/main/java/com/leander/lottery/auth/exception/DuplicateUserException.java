package com.leander.lottery.auth.exception;

/**
 * 當註冊時發現使用者名稱或 Email 已存在時拋出此異常
 */
public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String message) {
        super(message);
    }

    public DuplicateUserException(String message, Throwable cause) {
        super(message, cause);
    }
}