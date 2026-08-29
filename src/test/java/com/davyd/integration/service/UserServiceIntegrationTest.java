package com.davyd.integration.service;

import com.davyd.dto.response.UserResponse;
import com.davyd.exception.EmailAlreadyExistsException;
import com.davyd.exception.UserNotFoundException;
import com.davyd.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceIntegrationTest extends BaseServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUser(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        UserResponse userResponse = userService.createUser(name, email);

        assertNotNull(userResponse.id());
        assertEquals(email, userResponse.email());
        assertEquals(name, userResponse.name());
    }

    @Test
    void shouldThrowWhenCreatingUserWithSameEmail(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        userService.createUser(name, email);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser("Adri", email));
    }

    @Test
    void shouldGetUserById(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        UserResponse createdUser = userService.createUser(name, email);

        UserResponse gotUser = userService.getUser(createdUser.id());

        assertEquals(name, gotUser.name());
        assertEquals(email, gotUser.email());
    }

    @Test
    void shouldThrowWhenGettingNonExistentUserById(){
        assertThrows(UserNotFoundException.class, () ->
                userService.getUser(1L));
    }

    @Test
    void shouldGetUserByEmail(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        userService.createUser(name, email);

        UserResponse user = userService.getUser(email);

        assertNotNull(user.id());
        assertEquals(name, user.name());
        assertEquals(email, user.email());
    }

    @Test
    void shouldThrowWhenGettingNonExistentUserByEmail(){
        assertThrows(UserNotFoundException.class, () ->
                userService.getUser("dav@gmail.com"));
    }

    @Test
    void shouldChangeUserName(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        String newName = "Max";
        UserResponse userResponse = userService.createUser(name, email);

        userService.changeUserName(userResponse.id(), newName);

        UserResponse changedUser = userService.getUser(email);

        assertEquals(userResponse.id(), changedUser.id());
        assertEquals(email, changedUser.email());
        assertEquals(newName, changedUser.name());
    }

    @Test
    void shouldThrowWhenChangingNameOfNonExistentUser(){
        assertThrows(UserNotFoundException.class, () ->
                userService.changeUserName(1L, "newName"));
    }

    @Test
    void testExistById(){
        assertFalse(userService.existsById(1L));

        String name = "Davyd";
        String email = "davyd@gmail.com";
        UserResponse userResponse = userService.createUser(name, email);

        assertTrue(userService.existsById(userResponse.id()));
    }

    @Test
    void testExistByEmail(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        assertFalse(userService.existsByEmail(email));

        userService.createUser(name, email);

        assertTrue(userService.existsByEmail(email));
    }

    @Test
    void shouldDeleteUser(){
        String name = "Davyd";
        String email = "davyd@gmail.com";
        UserResponse userResponse = userService.createUser(name, email);

        userService.deleteUser(userResponse.id());

        assertFalse(userService.existsById(userResponse.id()));
    }

    @Test
    void shouldThrowWhenDeletingNonExistentUser(){
        assertThrows(UserNotFoundException.class, () ->
                userService.deleteUser(1L));
    }

    @Test
    void shouldGetAllUsers() {
        userService.createUser("Davyd", "davyd@gmail.com");
        userService.createUser("John", "john@gmail.com");

        Pageable pageable = PageRequest.of(0, 10);

        Page<UserResponse> result = userService.getAllUsers(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        assertTrue(result.getContent().stream()
                .anyMatch(user -> user.email().equals("davyd@gmail.com")));

        assertTrue(result.getContent().stream()
                .anyMatch(user -> user.email().equals("john@gmail.com")));
    }

    @Test
    void shouldGetUsersWithPagination() {
        userService.createUser("Davyd", "davyd@gmail.com");
        userService.createUser("John", "john@gmail.com");
        userService.createUser("Adriana", "adriana@gmail.com");

        Pageable pageable = PageRequest.of(
                0,
                2,
                Sort.by("id").ascending()
        );

        Page<UserResponse> result = userService.getAllUsers(pageable);

        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        assertEquals(2, result.getTotalPages());
        assertEquals(0, result.getNumber());
        assertTrue(result.hasNext());
    }
}
