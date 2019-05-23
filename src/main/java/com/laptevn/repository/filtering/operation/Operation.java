package com.laptevn.repository.filtering.operation;

import com.laptevn.ErrorMessages;
import com.laptevn.exception.IntegrityException;
import com.laptevn.auth.entity.Role;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Pattern;

public abstract class Operation {
    private final static String ENUM_FIELD_NAME = "role";
    private final static Pattern UNQUOTE_PATTERN = Pattern.compile("^\'|\'$");

    public <T> Predicate createPredicate(CriteriaBuilder criteriaBuilder, Root<T> entity, String identifierName, Object value) {
        if (identifierName.equalsIgnoreCase(ENUM_FIELD_NAME)) {
            String stringValue = value instanceof String
                    ? removeQuotes((String) value)
                    : value.toString();

            return executeRoleOperation(criteriaBuilder, entity.get(identifierName), getRole(stringValue));
        }

        if (value instanceof Integer) {
            return executeIntegerOperation(criteriaBuilder, entity.get(identifierName), (Integer) value);
        }

        if (value instanceof String) {
            try {
                return executeStringOperation(criteriaBuilder, entity.get(identifierName), removeQuotes((String) value));
            } catch (NumberFormatException ignore) {
                throw new IntegrityException(ErrorMessages.INCOMPATIBLE_VALUE_TYPE);
            }
        }

        if (value instanceof LocalDate) {
            return executeDateOperation(criteriaBuilder, entity.get(identifierName), (LocalDate) value);
        }

        if (value instanceof LocalTime) {
            return executeTimeOperation(criteriaBuilder, entity.get(identifierName), (LocalTime) value);
        }

        throw new IntegrityException(String.format(ErrorMessages.NOT_SUPPORTED_DATA_TYPE_FORMAT, value.getClass()));
    }

    private static Role getRole(String textValue) {
        try {
            return Role.valueOf(textValue);
        } catch (IllegalArgumentException ignore) {
            throw new IntegrityException(String.format(ErrorMessages.NOT_EXISTING_ROLE_FORMAT, textValue));
        }
    }

    public static String removeQuotes(String text) {
        return UNQUOTE_PATTERN.matcher(text).replaceAll("");
    }

    protected abstract Predicate executeRoleOperation(CriteriaBuilder criteriaBuilder, Expression<? extends Role> leftExpression, Role value);
    protected abstract Predicate executeIntegerOperation(CriteriaBuilder criteriaBuilder, Expression<? extends Integer> leftExpression, Integer value);
    protected abstract Predicate executeStringOperation(CriteriaBuilder criteriaBuilder, Expression<? extends String> leftExpression, String value);
    protected abstract Predicate executeDateOperation(CriteriaBuilder criteriaBuilder, Expression<? extends LocalDate> leftExpression, LocalDate value);
    protected abstract Predicate executeTimeOperation(CriteriaBuilder criteriaBuilder, Expression<? extends LocalTime> leftExpression, LocalTime value);

    public abstract OperationType getOperationType();
}