package com.laptevn.jogging.repository;

import com.laptevn.auth.entity.User;
import com.laptevn.jogging.entity.Jogging;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JoggingRepository extends CrudRepository<Jogging, Integer>, FilterableRepository {
    Optional<Jogging> findByIdAndUser(int id, User user);
    List<Jogging> findByUser(User user, Pageable pageable);
    Page<Jogging> findAll(Pageable pageable);
    List<Jogging> findByUser(User user);
    List<Jogging> findByDateBetweenAndUser(LocalDate leftBound, LocalDate rightBound, User user);
    List<Jogging> findByWeatherConditionIsNull();

    @Transactional
    int deleteByIdAndUser(int id, User user);
}