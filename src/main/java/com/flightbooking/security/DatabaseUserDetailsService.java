package com.flightbooking.security;

import com.flightbooking.model.AppUser;
import com.flightbooking.repository.UserRepository;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Locale;

@Service
public class DatabaseUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        try {
            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            AppUser user = userRepository.findByEmail(normalizedEmail).orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Invalid credentials")
            );

            return User.withUsername(user.email())
                    .password(user.passwordHash())
                    .roles("PASSENGER")
                    .disabled(!user.enabled())
                    .build();

        } catch (SQLException e) {
            throw new AuthenticationServiceException("Unable to access user database", e);
        }
    }
}
