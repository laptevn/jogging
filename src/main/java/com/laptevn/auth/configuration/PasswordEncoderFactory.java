package com.laptevn.auth.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderFactory {
    @Bean
    public PasswordEncoder createEncoder() {
        return new BCryptPasswordEncoder();
    }
}