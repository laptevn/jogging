package com.laptevn.jogging.repository;

import com.laptevn.auth.entity.User;
import com.laptevn.jogging.entity.Jogging;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FilterableRepository {
    List<Jogging> findByUser(User user, String filter, Optional<Pageable> pagination);
    List<Jogging> findAll(String filter, Optional<Pageable> pagination);
}