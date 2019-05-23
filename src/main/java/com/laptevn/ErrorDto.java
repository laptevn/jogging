package com.laptevn;

import org.springframework.http.HttpStatus;

public class ErrorDto {
    private final HttpStatus status;
    private final String message;

    public ErrorDto(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ErrorDto{" +
                "status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}