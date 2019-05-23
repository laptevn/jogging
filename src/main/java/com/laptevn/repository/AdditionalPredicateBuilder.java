package com.laptevn.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@FunctionalInterface
public interface AdditionalPredicateBuilder<T> {
    Predicate build(CriteriaBuilder criteriaBuilder, Root<T> entity);
}