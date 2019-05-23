package com.laptevn.repository.filtering;

import com.laptevn.ErrorMessages;
import com.laptevn.exception.IntegrityException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

class ExceptionErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e) {

        throw new IntegrityException(ErrorMessages.INVALID_FORMAT_WHERE_DETAILS + msg);
    }
}