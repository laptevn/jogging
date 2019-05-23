package com.laptevn.repository;

import com.laptevn.ErrorMessages;
import com.laptevn.exception.IntegrityException;
import com.laptevn.repository.filtering.WhereClauseBuilder;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public abstract class ParsingFilterableRepository<T> {
    private final EntityManager entityManager;
    private final WhereClauseBuilder whereClauseBuilder;

    protected ParsingFilterableRepository(EntityManager entityManager, WhereClauseBuilder whereClauseBuilder) {
        this.entityManager = entityManager;
        this.whereClauseBuilder = whereClauseBuilder;
    }

    protected List<T> find(
            String filter,
            Optional<Pageable> pagination,
            Class<T> entityClass,
            Optional<AdditionalPredicateBuilder> additionalPredicateBuilder) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<T> entity = criteriaQuery.from(entityClass);

        Predicate predicate = whereClauseBuilder.build(criteriaBuilder, entity, filter);
        if (predicate == null) {
            throw new IntegrityException(ErrorMessages.INVALID_FORMAT_WHERE);
        }

        if (additionalPredicateBuilder.isPresent()) {
            predicate = criteriaBuilder.and(additionalPredicateBuilder.get().build(criteriaBuilder, entity), predicate);
        }

        criteriaQuery.select(entity).where(predicate);
        Query query;
        try {
            query = entityManager.createQuery(criteriaQuery);
        } catch (IllegalArgumentException e) {
            throw new IntegrityException(ErrorMessages.INCOMPATIBLE_VALUE_TYPE);
        }

        if (pagination.isPresent()) {
            query.setFirstResult(pagination.get().getPageNumber() * pagination.get().getPageSize());
            query.setMaxResults(pagination.get().getPageSize());
        }

        return query.getResultList();
    }
}