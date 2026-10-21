package com.davyd.integration.controller;

import com.davyd.user.dto.request.ChangeUserNameRequest;
import com.davyd.integration.TestcontainersConfiguration;
import com.davyd.user.User;
import com.davyd.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import com.davyd.security.principal.FlowBankPrincipal;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldChangeUserName() throws Exception {
        User user = userRepository.save(
                new User("Davyd", "davyd@gmail.com")
        );

        ChangeUserNameRequest request =
                new ChangeUserNameRequest("Max");

        mockMvc.perform(patch("/users/me/name")
                        .with(user(new FlowBankPrincipal(user.getId(), "controller-test@example.com", "unused")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value("Max"))
                .andExpect(jsonPath("$.email").value("davyd@gmail.com"));

        User changedUser = userRepository.findById(user.getId())
                .orElseThrow();

        assertEquals("Max", changedUser.getName());
    }

    @Test
    void shouldReturn404WhenChangingNameOfNonExistingUser()
            throws Exception {

        ChangeUserNameRequest request =
                new ChangeUserNameRequest("Max");

        mockMvc.perform(patch("/users/me/name")
                        .with(user(new FlowBankPrincipal(999999L, "controller-test@example.com", "unused")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenChangingNameToInvalidName()
            throws Exception {

        User user = userRepository.save(
                new User("Davyd", "davyd@gmail.com")
        );

        ChangeUserNameRequest request =
                new ChangeUserNameRequest("");

        mockMvc.perform(patch("/users/me/name")
                        .with(user(new FlowBankPrincipal(user.getId(), "controller-test@example.com", "unused")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.createdAt").exists());

        User unchangedUser = userRepository.findById(user.getId())
                .orElseThrow();

        assertEquals("Davyd", unchangedUser.getName());
    }

    @Test
    void shouldDeleteUser() throws Exception {
        User user = userRepository.save(
                new User("Davyd", "davyd@gmail.com")
        );

        long userId = user.getId();

        mockMvc.perform(delete("/users/me")
                        .with(user(new FlowBankPrincipal(user.getId(), "controller-test@example.com", "unused"))).with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingUser() throws Exception {
        mockMvc.perform(delete("/users/me")
                        .with(user(new FlowBankPrincipal(999999L, "controller-test@example.com", "unused"))).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
