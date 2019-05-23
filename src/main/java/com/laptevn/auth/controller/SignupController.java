package com.laptevn.auth.controller;

import com.laptevn.ErrorDto;
import com.laptevn.ErrorMessages;
import com.laptevn.auth.entity.SignupDto;
import com.laptevn.auth.service.SignupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Validated
public class SignupController {
    private final static Logger logger = LoggerFactory.getLogger(SignupController.class);

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity signup(@Valid @RequestBody SignupDto signupDto) {
        logger.info("Signing up with '{}' user name", signupDto.getName());

        return signupService.createUser(signupDto)
                ? UserController.handleUserCreated(signupDto.getName())
                : ResponseEntity.unprocessableEntity().body(
                        new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, ErrorMessages.USER_ALREADY_EXISTS));
    }
}