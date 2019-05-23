package com.laptevn.repository.filtering;

import com.laptevn.repository.filtering.operation.Operation;
import com.laptevn.repository.filtering.operation.OperationType;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;
import java.util.Map;

@Component
class ExpressionBuildingVisitorFactory {
    public <T> ExpressionBuildingVisitor createVisitor(
            CriteriaBuilder criteriaBuilder, Root<T> entity, Map<OperationType, Operation> operations) {

        return new ExpressionBuildingVisitor(criteriaBuilder, entity, operations);
    }
}