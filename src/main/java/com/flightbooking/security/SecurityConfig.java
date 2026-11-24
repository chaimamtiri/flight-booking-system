package com.flightbooking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider) throws Exception {
        http.authenticationProvider(authenticationProvider)

                .authorizeHttpRequests(auth -> auth

                        // Public flight search
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/flights",
                                "/api/flights/**"
                        ).permitAll()

                        // Swagger - development environment
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Only an authenticated user can reach this endpoint
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/bookings/me"
                        ).authenticated()

                        // Other booking operations remain blocked
                        .requestMatchers(
                                "/api/bookings",
                                "/api/bookings/**",
                                "/api/passengers",
                                "/api/passengers/**"
                        ).denyAll()

                        .anyRequest().denyAll()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.disable());

        return http.build();
    }
}
