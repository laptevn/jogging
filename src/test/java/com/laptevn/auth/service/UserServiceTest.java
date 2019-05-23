package com.laptevn.auth.service;

import com.laptevn.exception.IntegrityException;
import com.laptevn.PaginationFactory;
import com.laptevn.auth.RoleSpringConverter;
import com.laptevn.auth.repository.UserRepository;
import com.laptevn.auth.entity.Role;
import com.laptevn.auth.entity.User;
import com.laptevn.auth.entity.UserDto;
import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserServiceTest {
    public static final UserDto USER_DTO = new UserDto()
            .setName("John")
            .setPassword("123")
            .setRole(Role.USER);

    private static final User USER = new User()
            .setName(USER_DTO.getName())
            .setPassword(USER_DTO.getPassword())
            .setRole(USER_DTO.getRole());

    @Test
    public void createUser() {
        UserRepository userRepository = EasyMock.mock(UserRepository.class);
        EasyMock.expect(userRepository.save(EasyMock.anyObject())).andReturn(null);
        EasyMock.replay(userRepository);

        assertTrue(new UserService(userRepository, createPasswordEncoder(), null, null)
                .createUser(USER_DTO));
    }

    @Test
    public void createUserWithException() {
        UserRepository userRepository = EasyMock.mock(UserRepository.class);
        EasyMock.expect(
                userRepository.save(EasyMock.anyObject())).andThrow(new DataIntegrityViolationException("Test exception"));
        EasyMock.replay(userRepository);

        assertFalse(new UserService(userRepository, createPasswordEncoder(), null, null)
                .createUser(USER_DTO));
    }

    private static PasswordEncoder createPasswordEncoder() {
        PasswordEncoder passwordEncoder = EasyMock.mock(PasswordEncoder.class);
        EasyMock.expect(passwordEncoder.encode(EasyMock.anyString())).andReturn(USER_DTO.getPassword());
        EasyMock.replay(passwordEncoder);
        return passwordEncoder;
    }

    @Test
    public void getExistingUser() {
        Optional<UserDto> actualUser = new UserService(
                createSearchableRepository(Optional.of(USER)), null, null, null)
                .getUser(USER_DTO.getName());

        assertTrue("User wasn't found", actualUser.isPresent());
        assertEquals("Invalid name of found user", USER_DTO.getName(), actualUser.get().getName());
        assertEquals("Password is not empty", null, actualUser.get().getPassword());
        assertEquals("Invalid role of found user", USER_DTO.getRole(), actualUser.get().getRole());
    }

    @Test
    public void getNonExistingUser() {
        Optional<UserDto> actualUser = new UserService(
                createSearchableRepository(Optional.empty()), null, null, null)
                .getUser(USER_DTO.getName());
        assertFalse(actualUser.isPresent());
    }

    private static UserRepository createSearchableRepository(Optional<User> foundUser) {
        UserRepository userRepository = EasyMock.mock(UserRepository.class);
        EasyMock.expect(
                userRepository.findByNameIgnoreCase(EasyMock.anyString())).andReturn(foundUser);
        EasyMock.expect(userRepository.save(EasyMock.anyObject())).andReturn(USER);
        EasyMock.replay(userRepository);
        return userRepository;
    }

    @Test
    public void deleteExistingUser() {
        assertTrue(new UserService(
                createDeletableRepository(1), null, null, null)
                .deleteUser(USER_DTO.getName()));
    }

    @Test
    public void deleteNonExistingUser() {
        assertFalse(new UserService(
                createDeletableRepository(0), null, null, null)
                .deleteUser(USER_DTO.getName()));
    }

    private static UserRepository createDeletableRepository(int deletedEntriesCount) {
        UserRepository userRepository = EasyMock.mock(UserRepository.class);
        EasyMock.expect(
                userRepository.deleteByNameIgnoreCase(USER_DTO.getName())).andReturn(deletedEntriesCount);
        EasyMock.replay(userRepository);
        return userRepository;
    }

    @Test
    public void updateExistingUser() {
        Optional<String> name = new UserService(
                createSearchableRepository(Optional.of(USER)), createPasswordEncoder(), null, null)
                .updateUser(USER_DTO, USER_DTO.getName());

        assertFalse(name.isPresent());
    }

    @Test
    public void updateNonExistingUser() {
        UserDto updatedEntry = new UserDto()
                .setName(USER_DTO.getName() + 1)
                .setPassword(USER_DTO.getPassword())
                .setRole(USER_DTO.getRole());

        Optional<String> name = new UserService(
                createSearchableRepository(Optional.empty()), createPasswordEncoder(), null, null)
                .updateUser(updatedEntry, USER_DTO.getName() + 1);

        assertTrue(name.isPresent());
    }

    @Test(expected = IntegrityException.class)
    public void updateNonExistingUserAmbiguousName() {
        new UserService(
                createSearchableRepository(Optional.empty()), createPasswordEncoder(), null, null)
                .updateUser(USER_DTO, USER_DTO.getName() + 1);
    }

    @Test
    public void getAllUsersEmptyRepository() {
        Collection<UserDto> foundUsers = new UserService(
                createSearchableGroupRepository(new ArrayList<>(), false), null, null, new PaginationFactory())
                .getAllUsers(null, null, null);
        assertTrue(foundUsers.isEmpty());
    }

    @Test
    public void getAllUsersPageable() {
        Iterable<User> users = Arrays.asList(
                USER,
                new User()
                        .setName(USER.getName() + 1)
                        .setPassword(USER.getPassword() + 1)
                        .setRole(USER.getRole()));
        List<UserDto> foundUsers = (List<UserDto>) new UserService(
                createSearchableGroupRepository(users, true), null, null, new PaginationFactory())
                .getAllUsers(1, 1, null);
        assertEquals("Not all users were found", 2, foundUsers.size());
        assertEquals("Invalid first user", USER.getName(), foundUsers.get(0).getName());
        assertEquals("Password is not empty for the first user", null, foundUsers.get(0).getPassword());
        assertEquals("Invalid second user", USER.getName() + 1, foundUsers.get(1).getName());
        assertEquals("Password is not empty for the second user", null, foundUsers.get(1).getPassword());
    }

    @Test
    public void getAllUsersNotPageable() {
        Iterable<User> users = Arrays.asList(
                USER,
                new User()
                        .setName(USER.getName() + 1)
                        .setPassword(USER.getPassword() + 1)
                        .setRole(USER.getRole()));
        List<UserDto> foundUsers = (List<UserDto>) new UserService(
                createSearchableGroupRepository(users, false), null, null, new PaginationFactory())
                .getAllUsers(null, null, null);
        assertEquals("Not all users were found", 2, foundUsers.size());
        assertEquals("Invalid first user", USER.getName(), foundUsers.get(0).getName());
        assertEquals("Invalid second user", USER.getName() + 1, foundUsers.get(1).getName());
    }

    private static UserRepository createSearchableGroupRepository(Iterable<User> foundUsers, boolean isPageable) {
        UserRepository userRepository = EasyMock.mock(UserRepository.class);

        if (isPageable) {
            EasyMock.expect(userRepository.findAll(EasyMock.<Pageable>anyObject())).andReturn(foundUsers);
        } else {
            EasyMock.expect(userRepository.findAll()).andReturn(foundUsers);
        }

        EasyMock.replay(userRepository);
        return userRepository;
    }

    @Test
    public void loadExistingUser() {
        UserDetails userDetails = new UserService(
                createSearchableRepository(Optional.of(USER)), null, new RoleSpringConverter(), null)
                .loadUserByUsername(USER.getName());
        assertEquals("Invalid user name", USER.getName(), userDetails.getUsername());
        assertEquals("Invalid password", USER.getPassword(), userDetails.getPassword());
        assertEquals(
                "Invalid role",
                "ROLE_USER",
                userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void loadNonExistingUser() {
        new UserService(createSearchableRepository(Optional.empty()), null, new RoleSpringConverter(), null)
                .loadUserByUsername(USER.getName());
    }
}