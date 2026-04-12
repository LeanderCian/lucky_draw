package com.leander.lottery.admin.exception;

/**
 * 超過抽獎次數上限
 */
public class ExceedMaxTriesException extends RuntimeException {
    public ExceedMaxTriesException(String message) {
        super(message);
    }

    public ExceedMaxTriesException(String message, Throwable cause) {
        super(message, cause);
    }
}