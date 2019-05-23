package com.laptevn.exception;

public class IntegrityException extends RuntimeException {
    private static final long serialVersionUID = 2483380110129420709L;

    public IntegrityException(String message) {
        super(message);
    }
}