package com.laptevn.auth.service;

import com.laptevn.auth.entity.Role;
import com.laptevn.auth.entity.SignupDto;
import com.laptevn.auth.entity.UserDto;
import org.easymock.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SignupServiceTest {
    @Test
    public void createUser() {
        SignupDto signupDto = new SignupDto()
                .setName("Michele")
                .setPassword("123");
        UserDto userDto = new UserDto()
                .setName(signupDto.getName())
                .setPassword(signupDto.getPassword())
                .setRole(Role.USER);

        UserService userService = EasyMock.mock(UserService.class);
        EasyMock.expect(userService.createUser(userDto)).andReturn(true);
        EasyMock.replay(userService);

        assertTrue(new SignupService(userService).createUser(signupDto));
    }
}