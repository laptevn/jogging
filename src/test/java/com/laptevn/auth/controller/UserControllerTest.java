package com.laptevn.auth.controller;

import com.laptevn.ErrorDto;
import com.laptevn.ErrorMessages;
import com.laptevn.auth.service.UserService;
import com.laptevn.auth.service.UserServiceTest;
import com.laptevn.auth.entity.UserDto;
import com.laptevn.exception.IntegrityException;
import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class UserControllerTest {
    @Test
    public void createUser() {
        ResponseEntity response = new UserController(createUpdatableUserService(true))
                .createUser(UserServiceTest.USER_DTO);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void createUserWithExistingName() {
        ResponseEntity response = new UserController(createUpdatableUserService(false))
                .createUser(UserServiceTest.USER_DTO);
        assertEquals("Invalid status code", HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Invalid error message", ErrorMessages.USER_ALREADY_EXISTS, ((ErrorDto)response.getBody()).getMessage());
    }

    private static UserService createUpdatableUserService(boolean isUserCreated) {
        UserService userService = EasyMock.mock(UserService.class);
        EasyMock.expect(userService.createUser(EasyMock.anyObject())).andReturn(isUserCreated);
        EasyMock.replay(userService);
        return userService;
    }

    @Test
    public void updateUser() {
        UserService userService = EasyMock.mock(UserService.class);
        EasyMock.expect(userService.updateUser(EasyMock.anyObject(), EasyMock.anyString())).andReturn(Optional.empty());
        EasyMock.replay(userService);

        ResponseEntity response = new UserController(userService).updateUser(null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void updateUserExistingName() {
        UserService userService = EasyMock.mock(UserService.class);
        EasyMock.expect(userService.updateUser(EasyMock.anyObject(), EasyMock.anyString()))
                .andThrow(new DataIntegrityViolationException("Test exception"));
        EasyMock.replay(userService);

        ResponseEntity response = new UserController(userService).updateUser(null, null);
        assertEquals("Invalid status code", HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Invalid error message", ErrorMessages.NEW_NAME_IS_USED, ((ErrorDto)response.getBody()).getMessage());
    }

    @Test
    public void updateUserIntegrityException() {
        UserService userService = EasyMock.mock(UserService.class);
        IntegrityException exception = new IntegrityException("Test exception");
        EasyMock.expect(userService.updateUser(EasyMock.anyObject(), EasyMock.anyString())).andThrow(exception);
        EasyMock.replay(userService);

        ResponseEntity response = new UserController(userService).updateUser(null, null);
        assertEquals("Invalid status code", HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Invalid error message", exception.getMessage(), ((ErrorDto)response.getBody()).getMessage());
    }

    @Test
    public void getExistingUser() {
        ResponseEntity<UserDto> response = new UserController(
                createSearchableUserService(Optional.of(UserServiceTest.USER_DTO)))
                .getUser(null);
        assertEquals("Invalid status code", HttpStatus.OK, response.getStatusCode());
        assertSame("Invalid user found", UserServiceTest.USER_DTO, response.getBody());
    }

    @Test
    public void getNonExistingUser() {
        ResponseEntity<UserDto> response = new UserController(
                createSearchableUserService(Optional.empty())).getUser(null);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    private static UserService createSearchableUserService(Optional<UserDto> foundUser) {
        UserService userService = EasyMock.mock(UserService.class);
        EasyMock.expect(userService.getUser(EasyMock.anyString())).andReturn(foundUser);
        EasyMock.replay(userService);
        return userService;
    }

    @Test
    public void deleteExistingUser() {
        ResponseEntity response = new UserController(
                createDeletableUserService(true))
                .deleteUser(null);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void deleteNonExistingUser() {
        ResponseEntity response = new UserController(
                createDeletableUserService(false))
                .deleteUser(null);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    private static UserService createDeletableUserService(boolean isUserDeleted) {
        UserService userService = EasyMock.mock(UserService.class);
        EasyMock.expect(userService.deleteUser(EasyMock.anyString())).andReturn(isUserDeleted);
        EasyMock.replay(userService);
        return userService;
    }

    @Test
    public void getAllUsers() {
        UserService userService = EasyMock.mock(UserService.class);
        List<UserDto> users = Arrays.asList(UserServiceTest.USER_DTO);
        EasyMock.expect(userService.getAllUsers(EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyString()))
                .andReturn(users);
        EasyMock.replay(userService);

        ResponseEntity<Collection<UserDto>> response = new UserController(userService)
                .getAllUsers(null, null, null);
        assertEquals("Invalid status code", HttpStatus.OK, response.getStatusCode());
        assertSame("Invalid users found", users, response.getBody());
    }
}