package com.leander.lottery.admin.exception;

/**
 * 中獎機率超過100%
 */
public class StockNotEnoughException extends RuntimeException {
    public StockNotEnoughException(String message) {
        super(message);
    }

    public StockNotEnoughException(String message, Throwable cause) {
        super(message, cause);
    }
}