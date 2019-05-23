package com.laptevn.repository.filtering;

import com.laptevn.jogging.WhereClauseLexer;
import com.laptevn.jogging.WhereClauseParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.stereotype.Component;

@Component
class WhereClauseParserFactory {
    public WhereClauseParser createParser(String expression) {
        WhereClauseLexer lexer = new WhereClauseLexer(CharStreams.fromString(expression));
        ExceptionErrorListener errorListener = new ExceptionErrorListener();
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        WhereClauseParser parser = new WhereClauseParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        return parser;
    }
}