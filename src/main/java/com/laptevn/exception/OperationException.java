package com.laptevn.exception;

public class OperationException extends RuntimeException {
    private static final long serialVersionUID = 5807854274611300887L;

    public OperationException(String message) {
        super(message);
    }
}