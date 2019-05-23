package com.laptevn.repository.filtering;

import com.laptevn.exception.IntegrityException;
import org.junit.Test;

public class ExceptionErrorListenerTest {
    @Test(expected = IntegrityException.class)
    public void handleError() {
        new ExceptionErrorListener().syntaxError(null, null, 0, 0, null, null);
    }
}