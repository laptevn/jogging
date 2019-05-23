package com.laptevn;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.validation.ConstraintViolationException;

import static org.junit.Assert.assertEquals;

public class ValidationExceptionHandlerTest {
    @Test
    public void constraintViolation() {
        String exceptionMessage = "Hola";
        ResponseEntity responseEntity = new ValidationExceptionHandler().exceptionHandler(
                new ConstraintViolationException(exceptionMessage, null));
        assertEquals("Invalid status code of response", HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Invalid body of response", exceptionMessage, ((ErrorDto) responseEntity.getBody()).getMessage());
    }
}