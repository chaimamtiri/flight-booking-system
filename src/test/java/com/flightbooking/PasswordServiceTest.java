
package com.flightbooking;

import com.flightbooking.security.PasswordService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    private final PasswordService passwordService =
            new PasswordService();

    @Test
    void shouldHashPassword() {

        String password = "ExamplePassword!2026";

        String hash = passwordService.hashPassword(password);

        assertNotEquals(password, hash);
        assertTrue(hash.startsWith("$2"));
    }

    @Test
    void shouldVerifyCorrectPassword() {

        String password = "ExamplePassword!2026";

        String hash = passwordService.hashPassword(password);

        assertTrue(
                passwordService.verifyPassword(password, hash)
        );
    }

    @Test
    void shouldRejectIncorrectPassword() {

        String hash = passwordService.hashPassword(
                "ExamplePassword!2026"
        );

        assertFalse(
                passwordService.verifyPassword(
                        "WrongPassword!",
                        hash
                )
        );
    }

    @Test
    void shouldRejectEmptyPassword() {

        assertThrows(
                IllegalArgumentException.class,
                () -> passwordService.hashPassword("")
        );
    }
}
