package com.laptevn.repository.filtering;

import com.laptevn.repository.filtering.operation.Operation;
import com.laptevn.repository.filtering.operation.OperationType;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Map;

@Component
public class WhereClauseBuilder {
    private final Map<OperationType, Operation> operations;
    private final WhereClauseParserFactory parserFactory;
    private final ExpressionBuildingVisitorFactory visitorFactory;

    public WhereClauseBuilder(
            Map<OperationType, Operation> operations,
            WhereClauseParserFactory parserFactory,
            ExpressionBuildingVisitorFactory visitorFactory) {

        this.operations = operations;
        this.parserFactory = parserFactory;
        this.visitorFactory = visitorFactory;
    }

    public <T> Predicate build(CriteriaBuilder criteriaBuilder, Root<T> entity, String expression) {
        return (Predicate) visitorFactory.createVisitor(criteriaBuilder, entity, operations)
                .visit(parserFactory.createParser(expression).parse());
    }
}