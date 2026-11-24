package com.flightbooking;

import com.flightbooking.model.AppUser;
import com.flightbooking.repository.PassengerRepository;
import com.flightbooking.repository.UserRepository;
import com.flightbooking.security.PasswordService;
import com.flightbooking.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PassengerRepository passengerRepository;
    private PasswordService passwordService;
    private UserService userService;

    @BeforeEach
    void setUp() {

        userRepository = mock(UserRepository.class);
        passengerRepository = mock(PassengerRepository.class);
        passwordService = new PasswordService();

        userService = new UserService(
                userRepository,
                passengerRepository,
                passwordService
        );
    }

    @Test
    void shouldCreateAccountWithHashedPassword()
            throws Exception {

        Passenger passenger = new Passenger(
                "P003",
                "Lina",
                "Ben Ali",
                "lina@example.com"
        );

        when(passengerRepository.findById("P003"))
                .thenReturn(passenger);

        when(userRepository.save(
                eq("P003"),
                eq("lina@example.com"),
                anyString()
        )).thenAnswer(invocation -> {

            String hash = invocation.getArgument(2);

            assertNotEquals("ExamplePassword!2026", hash);

            assertTrue(passwordService.verifyPassword(
                    "ExamplePassword!2026",
                    hash
            ));

            return new AppUser(
                    1L,
                    "P003",
                    "lina@example.com",
                    hash,
                    true
            );
        });

        AppUser created =
                userService.createAccountForVerifiedPassenger(
                        "P003",
                        "lina@example.com",
                        "ExamplePassword!2026"
                );

        assertEquals("P003", created.passengerId());
        assertEquals("lina@example.com", created.email());
    }

    @Test
    void shouldRejectEmailMismatch() throws Exception {

        Passenger passenger = new Passenger(
                "P003",
                "Lina",
                "Ben Ali",
                "lina@example.com"
        );

        when(passengerRepository.findById("P003"))
                .thenReturn(passenger);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createAccountForVerifiedPassenger(
                        "P003",
                        "another@example.com",
                        "ExamplePassword!2026"
                )
        );

        verify(userRepository, never()).save(
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    void shouldAuthenticateValidUser() throws Exception {

        String hash = passwordService.hashPassword(
                "ExamplePassword!2026"
        );

        AppUser user = new AppUser(
                1L,
                "P003",
                "lina@example.com",
                hash,
                true
        );

        when(userRepository.findByEmail(
                "lina@example.com"
        )).thenReturn(Optional.of(user));

        Optional<AppUser> authenticated =
                userService.authenticate(
                        "lina@example.com",
                        "ExamplePassword!2026"
                );

        assertTrue(authenticated.isPresent());
        assertEquals(
                "P003",
                authenticated.get().passengerId()
        );
    }

    @Test
    void shouldRejectWrongPassword() throws Exception {

        String hash = passwordService.hashPassword(
                "ExamplePassword!2026"
        );

        AppUser user = new AppUser(
                1L,
                "P003",
                "lina@example.com",
                hash,
                true
        );

        when(userRepository.findByEmail(
                "lina@example.com"
        )).thenReturn(Optional.of(user));

        assertTrue(userService.authenticate(
                "lina@example.com",
                "WrongPassword!"
        ).isEmpty());
    }

    @Test
    void shouldRejectDisabledAccount() throws Exception {

        String hash = passwordService.hashPassword(
                "ExamplePassword!2026"
        );

        AppUser user = new AppUser(
                1L,
                "P003",
                "lina@example.com",
                hash,
                false
        );

        when(userRepository.findByEmail(
                "lina@example.com"
        )).thenReturn(Optional.of(user));

        assertTrue(userService.authenticate(
                "lina@example.com",
                "ExamplePassword!2026"
        ).isEmpty());
    }

    @Test
    void shouldRejectShortPassword() {

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createAccountForVerifiedPassenger(
                        "P003",
                        "lina@example.com",
                        "short"
                )
        );
    }
}
