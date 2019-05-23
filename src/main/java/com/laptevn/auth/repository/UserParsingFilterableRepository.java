package com.laptevn.auth.repository;

import com.laptevn.auth.entity.User;
import com.laptevn.repository.filtering.WhereClauseBuilder;
import com.laptevn.repository.ParsingFilterableRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.Optional;

@Component("userRepositoryImpl")
public class UserParsingFilterableRepository extends ParsingFilterableRepository<User> implements FilterableRepository {
    public UserParsingFilterableRepository(EntityManager entityManager, WhereClauseBuilder whereClauseBuilder) {
        super(entityManager, whereClauseBuilder);
    }

    @Override
    public Iterable<User> findAll(String filter, Optional<Pageable> pagination) {
        return find(filter, pagination, User.class, Optional.empty());
    }
}