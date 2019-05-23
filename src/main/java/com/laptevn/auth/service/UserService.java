package com.laptevn.auth.service;

import com.laptevn.ErrorMessages;
import com.laptevn.exception.IntegrityException;
import com.laptevn.PaginationFactory;
import com.laptevn.auth.RoleSpringConverter;
import com.laptevn.auth.repository.UserRepository;
import com.laptevn.auth.entity.User;
import com.laptevn.auth.entity.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class UserService implements UserDetailsService {
    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleSpringConverter roleSpringConverter;
    private final PaginationFactory paginationFactory;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleSpringConverter roleSpringConverter, PaginationFactory paginationFactory) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleSpringConverter = roleSpringConverter;
        this.paginationFactory = paginationFactory;
    }

    public boolean createUser(UserDto userDto) {
        User user = new User()
                .setName(userDto.getName())
                .setPassword(passwordEncoder.encode(userDto.getPassword()))
                .setRole(userDto.getRole());

        try {
            userRepository.save(user);
            return true;
        } catch (DataIntegrityViolationException e) {
            logger.info(ErrorMessages.USER_ALREADY_EXISTS, e);
            return false;
        }
    }

    @Transactional
    public Optional<String> updateUser(UserDto userDto, String name) {
        Optional<User> foundUser = userRepository.findByNameIgnoreCase(name);
        User user;
        if (foundUser.isPresent()) {
            user = foundUser.get();
            logger.info("Updating existing user");
        } else {
            if (!name.equals(userDto.getName())) {
                throw new IntegrityException(ErrorMessages.UPDATE_USER_AMBIGUOUS_NAME);
            }

            user = new User();
            logger.info("Creating new user");
        }

        user
            .setName(userDto.getName())
            .setPassword(passwordEncoder.encode(userDto.getPassword()))
            .setRole(userDto.getRole());

        userRepository.save(user);

        return foundUser.isPresent() ? Optional.empty() : Optional.of(user.getName());
    }

    public boolean deleteUser(String name) {
        int deletedEntriesCount = userRepository.deleteByNameIgnoreCase(name);
        return deletedEntriesCount > 0;
    }

    public Optional<UserDto> getUser(String name) {
        Optional<User> foundUser = userRepository.findByNameIgnoreCase(name);
        return foundUser.map(user -> new UserDto()
                .setName(user.getName())
                .setRole(user.getRole()));
    }

    public Collection<UserDto> getAllUsers(Integer pageIndex, Integer pageSize, String filter) {
        Iterable<User> users;
        Optional<Pageable> pagination = paginationFactory.createPagination(pageIndex, pageSize);
        users = filter != null
                ? userRepository.findAll(filter, pagination)
                : pagination
                    .map(userRepository::findAll)
                    .orElseGet(userRepository::findAll);

        return StreamSupport.stream(users.spliterator(), false)
                .map(
                        user ->
                                new UserDto()
                                        .setName(user.getName())
                                        .setRole(user.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<User> foundUser = userRepository.findByNameIgnoreCase(username);
        if (!foundUser.isPresent()) {
            throw new UsernameNotFoundException(String.format(ErrorMessages.USER_WAS_NOT_FOUND_TEMPLATE, username));
        }

        User user = foundUser.get();
        return new org.springframework.security.core.userdetails.User(
                user.getName(), user.getPassword(), AuthorityUtils.createAuthorityList(roleSpringConverter.convert(user.getRole()))
        );
    }
}