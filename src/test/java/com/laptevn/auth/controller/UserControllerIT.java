package com.laptevn.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laptevn.ErrorDto;
import com.laptevn.ErrorMessages;
import com.laptevn.auth.service.UserServiceTest;
import com.laptevn.auth.entity.Role;
import com.laptevn.auth.entity.UserDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class UserControllerIT {
    private final static List<UserDto> PREDEFINED_USERS = Collections.unmodifiableList(Arrays.asList(
            new UserDto()
                    .setName("user1")
                    .setRole(Role.USER),
            new UserDto()
                    .setName("user2")
                    .setRole(Role.USER),
            new UserDto()
                    .setName("admin")
                    .setRole(Role.ADMIN)
    ));

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
    @WithMockUser(username = "user")
    public void getUsersForbidden() throws Exception {
        client.perform(get("/users/").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void createExistingUser() throws Exception {
        try {
            createUser(UserServiceTest.USER_DTO);

            ErrorDto errorDto = new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, ErrorMessages.USER_ALREADY_EXISTS);

            client.perform(
                    post("/users/")
                            .content(objectMapper.writeValueAsString(UserServiceTest.USER_DTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(content().json(objectMapper.writeValueAsString(errorDto)));
        } finally {
            deleteUser(UserServiceTest.USER_DTO.getName());
        }
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void getUsers() throws Exception {
        client.perform(get("/users/").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(PREDEFINED_USERS)));

        List<UserDto> users = createUserDtos(3);

        try {
            for (UserDto user : users) {
                createUser(user);
            }

            List<UserDto> expectedUsers = new ArrayList<>(PREDEFINED_USERS);
            expectedUsers.addAll(users
                    .stream()
                    .map(user -> new UserDto()
                            .setName(user.getName())
                            .setRole(user.getRole()))
                    .collect(Collectors.toList()));
            client.perform(get("/users/").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedUsers)));

            client.perform(get("/users/?page=1").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedUsers)));

            client.perform(get("/users/?page=2").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(new ArrayList<>())));

            client.perform(get("/users/?page=2&per_page=2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("(name ne 'user1') and (name ne 'user2') and (name ne 'admin')"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(Arrays.asList(expectedUsers.get(2 + PREDEFINED_USERS.size())))));

            expectedUsers = expectedUsers.subList(PREDEFINED_USERS.size(), 2 + PREDEFINED_USERS.size());
            client.perform(get("/users/?per_page=2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("(name ne 'user1') and (name ne 'user2') and (name ne 'admin')"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedUsers)));
        } finally {
            for (UserDto user : users) {
                deleteUser(user.getName());
            }
        }
    }

    private static List<UserDto> createUserDtos(int usersCount) {
        return IntStream.range(0, usersCount)
                .mapToObj(i -> new UserDto()
                        .setName(UserServiceTest.USER_DTO.getName() + i)
                        .setPassword(UserServiceTest.USER_DTO.getPassword())
                        .setRole(UserServiceTest.USER_DTO.getRole())).collect(Collectors.toList());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void getUsersInvalidPageIndex() throws Exception {
        client.perform(get("/users/?page=0").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void getUsersInvalidPageSize() throws Exception {
        client.perform(get("/users/?per_page=0").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void createUserInvalidName() throws Exception {
        UserDto userDto = new UserDto()
                .setPassword(UserServiceTest.USER_DTO.getPassword())
                .setRole(UserServiceTest.USER_DTO.getRole());

        client.perform(
                post("/users/")
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void createUserInvalidPassword() throws Exception {
        UserDto userDto = new UserDto()
                .setName(UserServiceTest.USER_DTO.getName())
                .setRole(UserServiceTest.USER_DTO.getRole());

        client.perform(
                post("/users/")
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void deleteNonExistingUser() throws Exception {
        client.perform(
                delete("/users/non_existing_user"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void getUser() throws Exception {
        client.perform(
                get("/users/" + UserServiceTest.USER_DTO.getName()))
                .andExpect(status().isNotFound());

        try {
            createUser(UserServiceTest.USER_DTO);

            UserDto expectedUser = new UserDto()
                    .setName(UserServiceTest.USER_DTO.getName())
                    .setRole(UserServiceTest.USER_DTO.getRole());
            client.perform(
                    get("/users/" + expectedUser.getName().toLowerCase()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(expectedUser)));
        } finally {
            deleteUser(UserServiceTest.USER_DTO.getName());
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserAmbiguousName() throws Exception {
        ErrorDto errorDto = new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, ErrorMessages.UPDATE_USER_AMBIGUOUS_NAME);

        client.perform(
                put("/users/non_existing_user")
                        .content(objectMapper.writeValueAsString(UserServiceTest.USER_DTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(objectMapper.writeValueAsString(errorDto)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateUserExistingName() throws Exception {
        List<UserDto> users = createUserDtos(2);

        try {
            for (UserDto user : users) {
                createUser(user);
            }

            UserDto newUser = new UserDto()
                    .setName(users.get(0).getName())
                    .setPassword(users.get(1).getPassword())
                    .setRole(users.get(1).getRole());

            ErrorDto errorDto = new ErrorDto(HttpStatus.UNPROCESSABLE_ENTITY, ErrorMessages.NEW_NAME_IS_USED);

            client.perform(
                    put("/users/" + users.get(1).getName())
                            .content(objectMapper.writeValueAsString(newUser))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(content().json(objectMapper.writeValueAsString(errorDto)));
        } finally {
            for (UserDto user : users) {
                deleteUser(user.getName());
            }
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateExistingUser() throws Exception {
        UserDto newUser = UserServiceTest.USER_DTO;
        try {
            createUser(UserServiceTest.USER_DTO);

            newUser = new UserDto()
                    .setName(UserServiceTest.USER_DTO.getName() + 1)
                    .setPassword(UserServiceTest.USER_DTO.getPassword() + 1)
                    .setRole(Role.ADMIN);
            client.perform(
                    put("/users/" + UserServiceTest.USER_DTO.getName())
                            .content(objectMapper.writeValueAsString(newUser))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        } finally {
            deleteUser(newUser.getName());
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void updateNonExistingUser() throws Exception {
        client.perform(
                get("/users/" + UserServiceTest.USER_DTO.getName()))
                .andExpect(status().isNotFound());

        client.perform(
                put("/users/" + UserServiceTest.USER_DTO.getName())
                        .content(objectMapper.writeValueAsString(UserServiceTest.USER_DTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        deleteUser(UserServiceTest.USER_DTO.getName());
    }

    private void createUser(UserDto userDto) throws Exception {
        client.perform(
                post("/users/")
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    private void deleteUser(String name) throws Exception {
        client.perform(
                delete("/users/" + name))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void getUsersWithFilteringAndInvalidComparatorExpression() throws Exception {
        ErrorDto errorDto = new ErrorDto(
                HttpStatus.BAD_REQUEST,
                "Invalid format of where clause expression. Details: token recognition error at: '='");

        client.perform(
                get("/users/")
                        .content("id = 2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorDto)));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void getUsersWithFilteringAndInvalidBinaryExpression() throws Exception {
        ErrorDto errorDto = new ErrorDto(
                HttpStatus.BAD_REQUEST,
                "Invalid format of where clause expression. Details: mismatched input 'ORE' expecting {<EOF>, AND, OR}");

        client.perform(
                get("/users/")
                        .content("id eq 2 ORE id eq 3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorDto)));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void getUsersWithFilteringAndNotClosedBracket() throws Exception {
        ErrorDto errorDto = new ErrorDto(
                HttpStatus.BAD_REQUEST,
                "Invalid format of where clause expression. Details: missing ')' at '<EOF>'");

        client.perform(
                get("/users/")
                        .content("(id eq 2 OR id eq 3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorDto)));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void getUsersWithFilteringAndNotQuotedString() throws Exception {
        ErrorDto errorDto = new ErrorDto(
                HttpStatus.BAD_REQUEST,
                "Invalid format of where clause expression. Details: mismatched input 'ADMIN' expecting {DECIMAL, TEXT, DATE, TIME}");

        client.perform(
                get("/users/")
                        .content("role eq ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorDto)));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void getUsersWithFilteringAndUnkownRole() throws Exception {
        ErrorDto errorDto = new ErrorDto(HttpStatus.BAD_REQUEST, "'rockstar' role doesn't exist");

        client.perform(
                get("/users/")
                        .content("role eq 'rockstar'")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorDto)));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void getUsersWithFilteringAndTextInsteadOfNumber() throws Exception {
        ErrorDto errorDto = new ErrorDto(HttpStatus.BAD_REQUEST, ErrorMessages.INCOMPATIBLE_VALUE_TYPE);

        client.perform(
                get("/users/")
                        .content("id eq 'text'")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(errorDto)));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void getUsersWithFilteringAndNumberInsteadOfText() throws Exception {
        client.perform(
                get("/users/")
                        .content("name eq 123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(new ArrayList<>())));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    public void getUsersWithFiltering() throws Exception {
        List<UserDto> users = createUserDtos(4);

        try {
            for (UserDto user : users) {
                createUser(user);
            }

            List<UserDto> createdUsers = users.stream()
                    .map(user -> new UserDto()
                            .setName(user.getName())
                            .setRole(user.getRole()))
                    .collect(Collectors.toList());

            client.perform(
                    get("/users/")
                            .content("(((version eq 0) AND (role eq 'USER')))")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(createdUsers)));

            client.perform(
                    get("/users/")
                            .content("role EQ 'MANAGER'")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(new ArrayList<>())));

            List<UserDto> allUsers = new ArrayList<>();
            allUsers.addAll(PREDEFINED_USERS);
            allUsers.addAll(createdUsers);

            client.perform(
                    get("/users/")
                            .content("role ne 'MANAGER'")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(allUsers)));

            client.perform(
                    get("/users/")
                            .content("(name eq 'John2') OR (name eq 'John0')")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            Arrays.asList(createdUsers.get(0), createdUsers.get(2)))));

            client.perform(
                    get("/users/?page=2&per_page=2")
                            .content("role EQ 'USER'")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(
                            Arrays.asList(createdUsers.get(2), createdUsers.get(3)))));
        } finally {
            for (UserDto user : users) {
                deleteUser(user.getName());
            }
        }
    }
}