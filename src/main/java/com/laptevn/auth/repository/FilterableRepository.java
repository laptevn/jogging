package com.laptevn.auth.repository;

import com.laptevn.auth.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FilterableRepository {
    Iterable<User> findAll(String filter, Optional<Pageable> pagination);
}