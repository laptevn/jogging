package com.laptevn.auth.entity;

import com.laptevn.ErrorMessages;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SignupDto {
    @NotNull
    @Size(min = 1, message = ErrorMessages.EMPTY_NAME)
    private String name;

    @NotNull
    @Size(min = 1, message = ErrorMessages.EMPTY_PASSWORD)
    private String password;

    public String getName() {
        return name;
    }

    public SignupDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SignupDto setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String toString() {
        return "SignupDto{" +
                "name='" + name + '\'' +
                '}';
    }
}