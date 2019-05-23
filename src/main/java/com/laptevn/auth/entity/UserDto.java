package com.laptevn.auth.entity;

import com.laptevn.ErrorMessages;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserDto {
    @NotNull
    @Size(min = 1, message = ErrorMessages.EMPTY_NAME)
    private String name;

    @NotNull
    @Size(min = 1, message = ErrorMessages.EMPTY_PASSWORD)
    private String password;

    private Role role = Role.USER;

    public String getName() {
        return name;
    }

    public UserDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserDto setPassword(String password) {
        this.password = password;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public UserDto setRole(Role role) {
        this.role = role;
        return this;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "name='" + name + '\'' +
                ", role=" + role +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserDto userDto = (UserDto) o;
        if (!name.equals(userDto.name)) {
            return false;
        }

        if (password != null ? !password.equals(userDto.password) : userDto.password != null) {
            return false;
        }

        return role == userDto.role;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + role.hashCode();
        return result;
    }
}