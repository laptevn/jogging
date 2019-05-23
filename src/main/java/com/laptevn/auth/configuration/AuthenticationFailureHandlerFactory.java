package com.laptevn.auth.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

@Configuration
public class AuthenticationFailureHandlerFactory {
    @Bean
    public AuthenticationFailureHandler createFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler();
    }
}