package com.laptevn.auth;

import com.laptevn.auth.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleSpringConverter {
    private final static String ROLE_PREFIX = "ROLE_";
    public final static String USER_ROLE = ROLE_PREFIX + "USER";
    public final static String MANAGER_ROLE = ROLE_PREFIX + "MANAGER";
    public final static String ADMIN_ROLE = ROLE_PREFIX + "ADMIN";

    public String convert(Role role) {
        return ROLE_PREFIX + role;
    }
}