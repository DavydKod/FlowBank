package com.davyd.unit.models;

import com.davyd.models.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUser() {
        User user = new User("Davyd", "davyd@gmail.com");

        assertEquals("Davyd", user.getName());
        assertEquals("davyd@gmail.com", user.getEmail());
        assertNull(user.getId());
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User(null, "davyd@gmail.com")
        );
    }

    @Test
    void shouldThrowWhenNameIsEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User("", "davyd@gmail.com")
        );
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User("   ", "davyd@gmail.com")
        );
    }

    @Test
    void shouldThrowWhenEmailIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User("Davyd", null)
        );
    }

    @Test
    void shouldThrowWhenEmailIsInvalid() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new User("Davyd", "invalid-email")
        );
    }

    @Test
    void shouldChangeName() {
        User user = new User("Davyd", "davyd@gmail.com");

        user.changeName("David");

        assertEquals("David", user.getName());
    }

    @Test
    void shouldThrowWhenChangingNameToNull() {
        User user = new User("Davyd", "davyd@gmail.com");

        assertThrows(
                IllegalArgumentException.class,
                () -> user.changeName(null)
        );
    }

    @Test
    void shouldThrowWhenChangingNameToEmpty() {
        User user = new User("Davyd", "davyd@gmail.com");

        assertThrows(
                IllegalArgumentException.class,
                () -> user.changeName("")
        );
    }

    @Test
    void shouldThrowWhenChangingNameToBlank() {
        User user = new User("Davyd", "davyd@gmail.com");

        assertThrows(
                IllegalArgumentException.class,
                () -> user.changeName("   ")
        );
    }
}