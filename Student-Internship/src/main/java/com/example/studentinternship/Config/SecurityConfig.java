package com.example.studentinternship.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.csrf().disable() // Disable CSRF protection for simplicity
                .authorizeRequests()
                .anyRequest().permitAll() // Allow all requests without authentication
                .and()
                .httpBasic().disable(); // Disable HTTP Basic Authentication
        return http.build();
    }
}
