package com.davyd.integration.controller;

import com.davyd.dto.request.ChangeUserNameRequest;
import com.davyd.dto.request.CreateUserRequest;
import com.davyd.integration.TestcontainersConfiguration;
import com.davyd.models.User;
import com.davyd.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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

    // =========================================================
    // GET /users
    // =========================================================

    @Test
    void shouldGetAllUsers() throws Exception {
        userRepository.save(
                new User("Davyd", "davyd@gmail.com")
        );

        userRepository.save(
                new User("Max", "max@gmail.com")
        );

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void shouldReturnEmptyPageWhenNoUsersExist() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldGetUsersWithCustomPagination() throws Exception {
        userRepository.save(new User("User1", "user1@gmail.com"));
        userRepository.save(new User("User2", "user2@gmail.com"));
        userRepository.save(new User("User3", "user3@gmail.com"));

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    // =========================================================
    // GET /users/{id}
    // =========================================================

    @Test
    void shouldGetUserById() throws Exception {
        User user = userRepository.save(
                new User("Davyd", "davyd@gmail.com")
        );

        mockMvc.perform(get("/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value("Davyd"))
                .andExpect(jsonPath("$.email").value("davyd@gmail.com"));
    }

    @Test
    void shouldReturn404WhenUserByIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/users/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenUserIdIsNotPositive() throws Exception {
        mockMvc.perform(get("/users/{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // =========================================================
    // GET /users/by-email
    // =========================================================

    @Test
    void shouldGetUserByEmail() throws Exception {
        User user = userRepository.save(
                new User("Davyd", "davyd@gmail.com")
        );

        mockMvc.perform(get("/users/by-email")
                        .param("email", "davyd@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value("Davyd"))
                .andExpect(jsonPath("$.email").value("davyd@gmail.com"));
    }

    @Test
    void shouldReturn404WhenUserByEmailDoesNotExist() throws Exception {
        mockMvc.perform(get("/users/by-email")
                        .param("email", "unknown@gmail.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenEmailParameterIsInvalid() throws Exception {
        mockMvc.perform(get("/users/by-email")
                        .param("email", "invalid-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenEmailParameterIsMissing() throws Exception {
        mockMvc.perform(get("/users/by-email"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // POST /users
    // =========================================================

    @Test
    void shouldCreateUser() throws Exception {
        CreateUserRequest request =
                new CreateUserRequest("Davyd", "davyd@gmail.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Davyd"))
                .andExpect(jsonPath("$.email").value("davyd@gmail.com"));

        assertTrue(userRepository.existsByEmail("davyd@gmail.com"));
    }

    @Test
    void shouldReturn400WhenCreatingUserWithInvalidEmail() throws Exception {
        CreateUserRequest request =
                new CreateUserRequest("Davyd", "davyd.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.createdAt").exists());

        assertFalse(userRepository.existsByEmail("davyd.com"));
    }

    @Test
    void shouldReturn400WhenCreatingUserWithInvalidName() throws Exception {
        CreateUserRequest request =
                new CreateUserRequest("", "davyd@gmail.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.createdAt").exists());

        assertFalse(userRepository.existsByEmail("davyd@gmail.com"));
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        userRepository.save(
                new User("Davyd", "davyd@gmail.com")
        );

        CreateUserRequest request =
                new CreateUserRequest("Max", "davyd@gmail.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        assertEquals(1, userRepository.count());
    }

    @Test
    void shouldReturn400WhenCreateUserBodyIsInvalidJson() throws Exception {
        String invalidJson = """
                {
                    "name": "Davyd",
                    "email":
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        assertEquals(0, userRepository.count());
    }

    // =========================================================
    // PATCH /users/{id}/name
    // =========================================================

    @Test
    void shouldChangeUserName() throws Exception {
        User user = userRepository.save(
                new User("Davyd", "davyd@gmail.com")
        );

        ChangeUserNameRequest request =
                new ChangeUserNameRequest("Max");

        mockMvc.perform(patch("/users/{id}/name", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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

        mockMvc.perform(patch("/users/{id}/name", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenChangingNameWithInvalidId()
            throws Exception {

        ChangeUserNameRequest request =
                new ChangeUserNameRequest("Max");

        mockMvc.perform(patch("/users/{id}/name", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturn400WhenChangingNameToInvalidName()
            throws Exception {

        User user = userRepository.save(
                new User("Davyd", "davyd@gmail.com")
        );

        ChangeUserNameRequest request =
                new ChangeUserNameRequest("");

        mockMvc.perform(patch("/users/{id}/name", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.createdAt").exists());

        User unchangedUser = userRepository.findById(user.getId())
                .orElseThrow();

        assertEquals("Davyd", unchangedUser.getName());
    }

    // =========================================================
    // DELETE /users/{id}
    // =========================================================

    @Test
    void shouldDeleteUser() throws Exception {
        User user = userRepository.save(
                new User("Davyd", "davyd@gmail.com")
        );

        long userId = user.getId();

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingUser() throws Exception {
        mockMvc.perform(delete("/users/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenDeletingUserWithInvalidId() throws Exception {
        mockMvc.perform(delete("/users/{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}