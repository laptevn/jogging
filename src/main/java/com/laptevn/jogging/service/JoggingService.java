package com.laptevn.jogging.service;

import com.laptevn.ErrorMessages;
import com.laptevn.exception.IntegrityException;
import com.laptevn.PaginationFactory;
import com.laptevn.auth.entity.Role;
import com.laptevn.auth.entity.User;
import com.laptevn.auth.repository.UserRepository;
import com.laptevn.jogging.entity.Jogging;
import com.laptevn.jogging.entity.JoggingDto;
import com.laptevn.jogging.repository.JoggingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class JoggingService {
    private final static Logger logger = LoggerFactory.getLogger(JoggingService.class);

    private final JoggingRepository joggingRepository;
    private final UserRepository userRepository;
    private final PaginationFactory paginationFactory;

    public JoggingService(JoggingRepository joggingRepository, UserRepository userRepository, PaginationFactory paginationFactory) {
        this.joggingRepository = joggingRepository;
        this.userRepository = userRepository;
        this.paginationFactory = paginationFactory;
    }

    public int createJogging(JoggingDto joggingDto, String userName) {
        Jogging jogging = new Jogging()
                .setDate(joggingDto.getDate())
                .setDistance(joggingDto.getDistance())
                .setLocation(joggingDto.getLocation())
                .setTime(joggingDto.getTime())
                .setUser(getUser(userName));

        try {
            joggingRepository.save(jogging);
            return jogging.getId();
        } catch (DataIntegrityViolationException e) {
            String error = String.format(ErrorMessages.NOT_EXISTING_USER_FORMAT, userName);
            logger.info(error, e);
            throw new IntegrityException(error);
        }
    }

    public Optional<JoggingDto> getJogging(int id, String userName) {
        User user = getUser(userName);
        Optional<Jogging> foundJogging = isAllowedToAccessAllRecords(user)
                ? joggingRepository.findById(id)
                : joggingRepository.findByIdAndUser(id, user);
        return foundJogging.map(JoggingDto::create);
    }

    private static boolean isAllowedToAccessAllRecords(User user) {
        return user.getRole() == Role.ADMIN;
    }

    public boolean deleteJogging(int id, String userName) {
        User user = getUser(userName);
        if (isAllowedToAccessAllRecords(user)) {
            try {
                joggingRepository.deleteById(id);
                return true;
            } catch (EmptyResultDataAccessException e) {
                return false;
            }
        }

        int deletedEntriesCount = joggingRepository.deleteByIdAndUser(id, user);
        return deletedEntriesCount > 0;
    }

    public Optional<Integer> updateJogging(int id, JoggingDto joggingDto, String userName) {
        User user = getUser(userName);
        Optional<Jogging> foundJogging = isAllowedToAccessAllRecords(user)
                ? joggingRepository.findById(id)
                : joggingRepository.findByIdAndUser(id, user);

        Jogging jogging;
        boolean isUpdate = foundJogging.isPresent();
        if (isUpdate) {
            logger.info("Updating existing jogging");
            jogging = foundJogging.get();

            boolean needToUpdateWeather = !jogging.getDate().equals(joggingDto.getDate())
                    || !jogging.getLocation().equals(joggingDto.getLocation());
            if (needToUpdateWeather) {
                jogging.setWeatherCondition(null);
                jogging.setAverageTemperature(null);
            }
        } else {
            logger.info("Creating new jogging");
            jogging = new Jogging();
            jogging.setUser(user);
        }

        jogging
                .setDate(joggingDto.getDate())
                .setDistance(joggingDto.getDistance())
                .setLocation(joggingDto.getLocation())
                .setTime(joggingDto.getTime());

        try {
            joggingRepository.save(jogging);
        } catch (DataIntegrityViolationException e) {
            String error = String.format(ErrorMessages.NOT_EXISTING_USER_FORMAT, userName);
            logger.info(error, e);
            throw new IntegrityException(error);
        }

        return foundJogging.isPresent() ? Optional.empty() : Optional.of(jogging.getId());
    }

    public Collection<JoggingDto> getAllJoggings(String userName, Integer pageIndex, Integer pageSize, String filter) {
        Iterable<Jogging> joggings = getJoggings(
                paginationFactory.createPagination(pageIndex, pageSize), getUser(userName), filter);
        return StreamSupport.stream(joggings.spliterator(), false)
                .map(JoggingDto::create)
                .collect(Collectors.toList());
    }

    private Iterable<Jogging> getJoggings(Optional<Pageable> pagination, User user, String filter) {
        if (filter != null) {
            return isAllowedToAccessAllRecords(user)
                    ? joggingRepository.findAll(filter, pagination)
                    : joggingRepository.findByUser(user, filter, pagination);
        }

        if (pagination.isPresent()) {
            return isAllowedToAccessAllRecords(user)
                    ? joggingRepository.findAll(pagination.get())
                    : joggingRepository.findByUser(user, pagination.get());
        }

        return isAllowedToAccessAllRecords(user)
                ? joggingRepository.findAll()
                : joggingRepository.findByUser(user);
    }

    private User getUser(String userName) {
        return getUser(userRepository, userName);
    }

    public static User getUser(UserRepository userRepository, String userName) {
        Optional<User> user = userRepository.findByNameIgnoreCase(userName);
        if (!user.isPresent()) {
            throw new IntegrityException(String.format(ErrorMessages.NOT_EXISTING_USER_FORMAT, userName));
        }
        return user.get();
    }
}