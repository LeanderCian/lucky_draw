package com.leander.lottery.lottery.exception;

/**
 * 中獎機率超過100%
 */
public class RemainingCountNotEnoughException extends RuntimeException {
    public RemainingCountNotEnoughException(String message) {
        super(message);
    }

    public RemainingCountNotEnoughException(String message, Throwable cause) {
        super(message, cause);
    }
}