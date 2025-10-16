package com.example.studentinternship.Config;

import com.example.studentinternship.Entity.UserEntity;
import com.example.studentinternship.Repository.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class CustomJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    public CustomJwtAuthConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Extract email from Keycloak JWT
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("JWT does not contain a valid email claim");
        }

        // Look for user in local DB
        Optional<UserEntity> optionalUser = userRepository.findByEmail(email);
        UserEntity user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            System.out.println("[Auth] Existing user found in DB: " + user.getEmail());
        } else {
            // 👇 Lazy creation for first-time login
            user = new UserEntity();
            user.setEmail(email);
            user.setRole("student"); // Default role
            user = userRepository.save(user);
            System.out.println("[Auth] New user auto-created in DB: " + email);
        }

        // Convert DB role into GrantedAuthority
        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole())
        );

        return new JwtAuthenticationToken(jwt, authorities);
    }
}
