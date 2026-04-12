package com.leander.lottery.admin.exception;

/**
 * 中獎機率超過100%
 */
public class ProbabilityExceededException extends RuntimeException {
    public ProbabilityExceededException(String message) {
        super(message);
    }

    public ProbabilityExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}