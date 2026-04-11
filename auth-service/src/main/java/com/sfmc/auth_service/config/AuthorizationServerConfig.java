package com.sfmc.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.sfmc.auth_service.entity.User;
import com.sfmc.auth_service.repository.UserRepository;

import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class AuthorizationServerConfig {

    private final UserRepository userRepository;

    // Injection propre du repository
    public AuthorizationServerConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http) throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http
            .securityMatcher(
                    authorizationServerConfigurer
                            .getEndpointsMatcher())
            .with(
                    authorizationServerConfigurer,
                    configurer -> {}
            );

        return http.build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {

        return context -> {

            if (context.getPrincipal() != null) {

                User user = userRepository
                        .findByUsername(
                                context.getPrincipal().getName()
                        )
                        .orElseThrow();

                // Extraire les noms des rôles
                var roles = user.getRoles()
                        .stream()
                        .map(role -> role.getName())
                        .toList();

                // Ajouter dans JWT
                context.getClaims().claim(
                        "roles",
                        roles
                );
            }
        };
    }
}