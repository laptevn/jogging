package com.laptevn.jogging.controller;

import com.laptevn.ErrorDto;
import com.laptevn.ErrorMessages;
import com.laptevn.exception.IntegrityException;
import com.laptevn.auth.RoleSpringConverter;
import com.laptevn.jogging.service.JoggingService;
import com.laptevn.jogging.entity.JoggingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.security.Principal;
import java.util.Collection;
import java.util.Optional;

@RestController
@Validated
@Secured({RoleSpringConverter.USER_ROLE, RoleSpringConverter.ADMIN_ROLE})
public class JoggingController {
    private final static Logger logger = LoggerFactory.getLogger(JoggingController.class);

    private final JoggingService joggingService;

    public JoggingController(JoggingService joggingService) {
        this.joggingService = joggingService;
    }

    @RequestMapping(value = "/joggings/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createJogging(@Valid @RequestBody JoggingDto joggingDto, Principal principal) {
        logger.info("Creating new jogging '{}'", joggingDto);

        try {
            return createCreatedResponse(joggingService.createJogging(joggingDto, principal.getName()));
        } catch (IntegrityException e) {
            logger.info(e.getMessage());
            return ResponseEntity.unprocessableEntity().body(
                    new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage()));
        }
    }

    private static ResponseEntity createCreatedResponse(int id) {
        return ResponseEntity.created(URI.create("/joggings/" + id)).build();
    }

    @RequestMapping(value = "/joggings/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getJogging(@PathVariable int id, Principal principal) {
        logger.info("Getting jogging with '{}' id", id);

        Optional<JoggingDto> jogging;

        try {
            jogging = joggingService.getJogging(id, principal.getName());
        } catch (IntegrityException e) {
            logger.info(e.getMessage());
            return ResponseEntity.unprocessableEntity().body(
                    new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage()));
        }

        if (!jogging.isPresent()) {
            logger.info("Jogging wasn't found");
            return ResponseEntity.notFound().build();
        }

        logger.info("Jogging was found");
        return ResponseEntity.ok(jogging.get());
    }

    @RequestMapping(value = "/joggings/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteJogging(@PathVariable int id, Principal principal) {
        boolean isDeleted;
        try {
            isDeleted = joggingService.deleteJogging(id, principal.getName());
        } catch (IntegrityException e) {
            logger.info(e.getMessage());
            return ResponseEntity.unprocessableEntity().body(
                    new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage()));
        }

        if (!isDeleted) {
            logger.info("Cannot delete jogging with '{}' id, because it doesn't exist", id);
            return ResponseEntity.notFound().build();
        }

        logger.info("Jogging with '{}' id was deleted", id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/joggings/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateJogging(
            @PathVariable
            int id,

            @Valid
            @RequestBody
            JoggingDto joggingDto,
            Principal principal) {

        logger.info("Updating jogging with '{}' id", id);
        Optional<Integer> newId;
        try {
            newId = joggingService.updateJogging(id, joggingDto, principal.getName());
        } catch (IntegrityException e) {
            logger.info(e.getMessage());
            return ResponseEntity.unprocessableEntity().body(
                    new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage()));
        }

        return newId
                .map(JoggingController::createCreatedResponse)
                .orElseGet(() -> ResponseEntity.ok().build());
    }

    @RequestMapping(value = "/joggings/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllJoggings(
            @RequestParam(value = "page", required = false)
            @Min(value = 1, message = ErrorMessages.INVALID_PAGE_INDEX)
            Integer pageIndex,

            @RequestParam(value = "per_page", required = false)
            @Min(value = 1, message = ErrorMessages.INVALID_PAGE_SIZE)
            Integer pageSize,

            @RequestBody(required = false)
            String filter,
            Principal principal) {

        Collection<JoggingDto> joggings;
        try {
            joggings = joggingService.getAllJoggings(principal.getName(), pageIndex, pageSize, filter);
        } catch (IntegrityException e) {
            logger.info(e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()));
        }

        logger.info(
                "Getting all joggings. Found {} joggings. Page '{}', Per page '{}'. Filter '{}'",
                joggings.size(),
                pageIndex,
                pageSize,
                filter);
        return ResponseEntity.ok(joggings);
    }
}