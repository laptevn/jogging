package com.laptevn.jogging.repository;

import com.laptevn.auth.entity.User;
import com.laptevn.repository.AdditionalPredicateBuilder;
import com.laptevn.repository.ParsingFilterableRepository;
import com.laptevn.repository.filtering.WhereClauseBuilder;
import com.laptevn.jogging.entity.Jogging;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Component("joggingRepositoryImpl")
public class JoggingParsingFilterableRepository extends ParsingFilterableRepository<Jogging> implements FilterableRepository {
    public JoggingParsingFilterableRepository(EntityManager entityManager, WhereClauseBuilder whereClauseBuilder) {
        super(entityManager, whereClauseBuilder);
    }

    @Override
    public List<Jogging> findByUser(User user, String filter, Optional<Pageable> pagination) {
        return find(
                filter,
                pagination,
                Jogging.class,
                Optional.of((AdditionalPredicateBuilder<Jogging>)
                        (criteriaBuilder, entity) -> criteriaBuilder.equal(entity.get("user"), user)));
    }

    @Override
    public List<Jogging> findAll(String filter, Optional<Pageable> pagination) {
        return find(filter, pagination, Jogging.class, Optional.empty());
    }
}