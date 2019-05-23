package com.laptevn.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laptevn.ErrorDto;
import com.laptevn.ErrorMessages;
import com.laptevn.auth.entity.SignupDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class SignupControllerIT {
    private MockMvc client;
    private ObjectMapper objectMapper;

    @Autowired
    public void setClient(MockMvc client) {
        this.client = client;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Test
    public void signupNewUser() throws Exception {
        SignupDto signupDto = new SignupDto()
                .setName("new_user")
                .setPassword("123");

        client.perform(
                post("/signup")
                        .content(objectMapper.writeValueAsString(signupDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void signupNewUserWithInvalidFormat() throws Exception {
        SignupDto signupDto = new SignupDto()
                .setName("new user")
                .setPassword("123");

        client.perform(
                post("/signup")
                        .content(objectMapper.writeValueAsString(signupDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void signupExistingUser() throws Exception {
        SignupDto signupDto = new SignupDto()
                .setName("user1")
                .setPassword("123");

        ErrorDto errorDto = new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, ErrorMessages.USER_ALREADY_EXISTS);

        client.perform(
                post("/signup")
                        .content(objectMapper.writeValueAsString(signupDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(objectMapper.writeValueAsString(errorDto)));
    }

    @Test
    public void signupInvalidName() throws Exception {
        SignupDto signupDto = new SignupDto()
                .setName("")
                .setPassword("123");

        client.perform(
                post("/signup")
                        .content(objectMapper.writeValueAsString(signupDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void signupInvalidPassword() throws Exception {
        SignupDto signupDto = new SignupDto()
                .setName("123")
                .setPassword("");

        client.perform(
                post("/signup")
                        .content(objectMapper.writeValueAsString(signupDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}