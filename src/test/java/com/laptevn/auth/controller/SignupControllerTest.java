package com.laptevn.auth.controller;

import com.laptevn.ErrorDto;
import com.laptevn.ErrorMessages;
import com.laptevn.auth.entity.SignupDto;
import com.laptevn.auth.service.SignupService;
import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class SignupControllerTest {
    @Test
    public void signupNewUser() {
        SignupDto signupDto = new SignupDto().setName("test");
        ResponseEntity response = new SignupController(createService(true)).signup(signupDto);
        assertEquals("Invalid status code", HttpStatus.CREATED, response.getStatusCode());
        assertEquals(
                "Invalid location header",
                "/users/" + signupDto.getName(),
                response.getHeaders().getFirst("Location"));
    }

    @Test
    public void signupExistingUser() {
        SignupDto signupDto = new SignupDto().setName("test");
        ResponseEntity response = new SignupController(createService(false)).signup(signupDto);
        assertEquals("Invalid status code", HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Invalid error message", ErrorMessages.USER_ALREADY_EXISTS, ((ErrorDto) response.getBody()).getMessage());
    }

    private static SignupService createService(boolean isUserCreated) {
        SignupService signupService = EasyMock.mock(SignupService.class);
        EasyMock.expect(signupService.createUser(EasyMock.anyObject())).andReturn(isUserCreated);
        EasyMock.replay(signupService);
        return signupService;
    }
}