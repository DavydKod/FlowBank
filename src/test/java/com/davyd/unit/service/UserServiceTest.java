package com.davyd.unit.service;

import com.davyd.dto.response.UserResponse;
import com.davyd.exception.EmailAlreadyExistsException;
import com.davyd.exception.UserNotFoundException;
import com.davyd.models.User;
import com.davyd.repository.BankAccountRepository;
import com.davyd.repository.UserRepository;
import com.davyd.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldGetUserByEmail(){
        User user = new User("Davyd", "davyd@gmail.com");
        when(userRepository.findByEmail("davyd@gmail.com"))
                .thenReturn(Optional.of(user));

        UserResponse result = userService.getUser("davyd@gmail.com");

        assertEquals(user.getName(), result.name());
        assertEquals(user.getEmail(), result.email());
    }

    @Test
    void shouldThrowWhenUserNotFoundByEmail(){
        when(userRepository.findByEmail("dav@gmail.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUser("dav@gmail.com"));
    }

    @Test
    void shouldDeleteExistingUser(){
        User user = new User("Davyd", "davyd@gmail.com");
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowWhenDeletingNonExistingUser(){
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void shouldCreateUser() {
        when(userRepository.existsByEmail("davyd@gmail.com"))
                .thenReturn(false);

        when(userRepository.saveAndFlush(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result =
                userService.createUser("Davyd", "davyd@gmail.com");

        assertEquals("Davyd", result.name());
        assertEquals("davyd@gmail.com", result.email());

        verify(userRepository).existsByEmail("davyd@gmail.com");

        verify(userRepository).saveAndFlush(argThat(user ->
                user.getName().equals("Davyd")
                        && user.getEmail().equals("davyd@gmail.com")
        ));
    }

    @Test
    void shouldThrowWhenCreatingExistingUser(){
        User user = new User("Davyd", "davyd@gmail.com");
        when(userRepository.existsByEmail("davyd@gmail.com"))
                .thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(user.getName(), user.getEmail()));
    }

}
