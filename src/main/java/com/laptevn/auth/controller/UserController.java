package com.laptevn.auth.controller;

import com.laptevn.ErrorDto;
import com.laptevn.ErrorMessages;
import com.laptevn.exception.IntegrityException;
import com.laptevn.auth.RoleSpringConverter;
import com.laptevn.auth.service.UserService;
import com.laptevn.auth.entity.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;

@RestController
@Validated
@Secured({RoleSpringConverter.MANAGER_ROLE, RoleSpringConverter.ADMIN_ROLE})
public class UserController {
    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/users/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createUser(@Valid @RequestBody UserDto userDto) {
        logger.info("Creating new user with '{}' name", userDto.getName());
        return userService.createUser(userDto)
                ? handleUserCreated(userDto.getName())
                : ResponseEntity.unprocessableEntity().body(
                        new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, ErrorMessages.USER_ALREADY_EXISTS));

    }

    static ResponseEntity handleUserCreated(String userName) {
        URI locationHeader;
        try {
            locationHeader = createLocationHeader(userName);
        } catch (IntegrityException e) {
            logger.info(e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

        return ResponseEntity.created(locationHeader).build();
    }

    private static URI createLocationHeader(String userName) {
        try {
            return URI.create("/users/" + userName);
        } catch (IllegalArgumentException e) {
            throw new IntegrityException(ErrorMessages.INVALID_USER);
        }
    }

    @RequestMapping(value = "/users/{name}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateUser(
            @PathVariable
            String name,

            @Valid
            @RequestBody
            UserDto userDto) {

        logger.info("Updating user with '{}' name", name);
        Optional<String> newName;
        try {
            newName = userService.updateUser(userDto, name);
        } catch (DataIntegrityViolationException e) {
            logger.info(ErrorMessages.NEW_NAME_IS_USED, e);
            return ResponseEntity.unprocessableEntity().body(
                    new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, ErrorMessages.NEW_NAME_IS_USED));
        } catch (IntegrityException e) {
            logger.info(e.getMessage());
            return ResponseEntity.unprocessableEntity().body(
                    new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage()));
        }

        return newName
                .map(UserController::handleUserCreated)
                .orElseGet(() -> ResponseEntity.ok().build());
    }

    @RequestMapping(value = "/users/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getUser(@PathVariable String name) {
        logger.info("Getting user with '{}' name", name);
        Optional<UserDto> user = userService.getUser(name);
        if (!user.isPresent()) {
            logger.info("User wasn't found");
            return ResponseEntity.notFound().build();
        }

        logger.info("User was found");
        return ResponseEntity.ok(user.get());
    }

    @RequestMapping(value = "/users/{name}", method = RequestMethod.DELETE)
    public ResponseEntity deleteUser(@PathVariable String name) {
        boolean isUserDeleted = userService.deleteUser(name);
        if (!isUserDeleted) {
            logger.info("Cannot delete '{}' user because it doesn't exist", name);
            return ResponseEntity.notFound().build();
        }

        logger.info("'{}' user was deleted", name);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/users/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllUsers(
            @RequestParam(value = "page", required = false)
            @Min(value = 1, message = ErrorMessages.INVALID_PAGE_INDEX)
            Integer pageIndex,

            @RequestParam(value = "per_page", required = false)
            @Min(value = 1, message = ErrorMessages.INVALID_PAGE_SIZE)
            Integer pageSize,

            @RequestBody(required = false)
            String filter) {

        Collection<UserDto> users;
        try {
            users = userService.getAllUsers(pageIndex, pageSize, filter);
        } catch (IntegrityException e) {
            logger.info(e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

        logger.info(
                "Getting all users. Found {} users. Page '{}', Per page '{}'. Filter '{}'",
                users.size(),
                pageIndex,
                pageSize,
                filter);
        return ResponseEntity.ok(users);
    }
}