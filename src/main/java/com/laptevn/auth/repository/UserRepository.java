package com.laptevn.auth.repository;

import com.laptevn.auth.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Integer>, FilterableRepository {
    Optional<User> findByNameIgnoreCase(String name);

    @Transactional
    int deleteByNameIgnoreCase(String name);

    Iterable<User> findAll(Pageable pageable);
}