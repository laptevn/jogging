package com.laptevn.repository.filtering;

import com.laptevn.jogging.WhereClauseBaseVisitor;
import com.laptevn.jogging.WhereClauseParser;
import com.laptevn.ErrorMessages;
import com.laptevn.exception.IntegrityException;
import com.laptevn.repository.filtering.operation.Operation;
import com.laptevn.repository.filtering.operation.OperationType;
import org.antlr.v4.runtime.tree.RuleNode;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

class ExpressionBuildingVisitor<T> extends WhereClauseBaseVisitor<Object> {
    private final CriteriaBuilder criteriaBuilder;
    private final Root<T> entity;
    private final Map<OperationType, Operation> operations;

    public ExpressionBuildingVisitor(
            CriteriaBuilder criteriaBuilder, Root<T> entity, Map<OperationType, Operation> operations) {

        this.criteriaBuilder = criteriaBuilder;
        this.entity = entity;
        this.operations = operations;
    }

    @Override
    public Object visitBinaryExpression(WhereClauseParser.BinaryExpressionContext ctx) {
        if (ctx.op.AND() != null) {
            return criteriaBuilder.and((Expression<Boolean>) visit(ctx.left), (Expression<Boolean>) visit(ctx.right));
        }

        if (ctx.op.OR() != null) {
            return criteriaBuilder.or((Expression<Boolean>) visit(ctx.left), (Expression<Boolean>) visit(ctx.right));
        }

        throw new IntegrityException(ErrorMessages.INVALID_FORMAT_WHERE);
    }

    @Override
    public Object visitComparatorExpression(WhereClauseParser.ComparatorExpressionContext context) {
        String identifierName = context.left.getText();
        Object value = visit(context.right);
        if (context.op.EQ() != null) {
            return operations.get(OperationType.EQ).createPredicate(criteriaBuilder, entity, identifierName, value);
        }

        if (context.op.NE() != null) {
            return operations.get(OperationType.NE).createPredicate(criteriaBuilder, entity, identifierName, value);
        }

        if (context.op.GT() != null) {
            return operations.get(OperationType.GT).createPredicate(criteriaBuilder, entity, identifierName, value);
        }

        if (context.op.LT() != null) {
            return operations.get(OperationType.LT).createPredicate(criteriaBuilder, entity, identifierName, value);
        }

        throw new IntegrityException(ErrorMessages.INVALID_FORMAT_WHERE);
    }

    @Override
    public Object visitDecimalValue(WhereClauseParser.DecimalValueContext ctx) {
        try {
            return Integer.parseInt(ctx.DECIMAL().getText());
        } catch (NumberFormatException ignore) {
            throw new IntegrityException(ErrorMessages.INCOMPATIBLE_VALUE_TYPE);
        }
    }

    @Override
    public Object visitTextValue(WhereClauseParser.TextValueContext ctx) {
        return ctx.TEXT().getText();
    }

    @Override
    public Object visitDateValue(WhereClauseParser.DateValueContext ctx) {
        try {
            return LocalDate.parse(Operation.removeQuotes(ctx.DATE().getText()));
        } catch (DateTimeParseException e) {
            throw new IntegrityException(ErrorMessages.INCOMPATIBLE_VALUE_TYPE);
        }
    }

    @Override
    public Object visitTimeValue(WhereClauseParser.TimeValueContext ctx) {
        try {
            return LocalTime.parse(Operation.removeQuotes(ctx.TIME().getText()));
        } catch (DateTimeParseException e) {
            throw new IntegrityException(ErrorMessages.INCOMPATIBLE_VALUE_TYPE);
        }
    }

    @Override
    protected boolean shouldVisitNextChild(RuleNode node, Object currentResult) {
        boolean isNextRootExpression = currentResult != null;
        if (isNextRootExpression) {
            return false;
        }

        return super.shouldVisitNextChild(node, currentResult);
    }
}