package com.davyd.integration.controller;

import com.davyd.auth.AuthService;
import com.davyd.auth.UserCredentialsRepository;
import com.davyd.auth.dto.request.LoginRequest;
import com.davyd.auth.dto.request.RegisterRequest;
import com.davyd.integration.TestcontainersConfiguration;
import com.davyd.user.UserRepository;
import com.davyd.user.dto.response.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthControllerIT {
    private static final String EMAIL = "tester@example.com";
    private static final String PASSWORD = "long-enough-password";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserCredentialsRepository credentialsRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanUp() {
        credentialsRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldExposeCsrfTokenWithoutAuthentication() throws Exception {
        CsrfSession csrf = getCsrf(new MockHttpSession());

        assertFalse(csrf.token().isBlank());
        assertFalse(csrf.headerName().isBlank());
        assertFalse(csrf.parameterName().isBlank());
        mockMvc.perform(get("/auth/me").session(csrf.session()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRegisterHashPasswordAndAuthenticateSession() throws Exception {
        CsrfSession csrf = getCsrf(new MockHttpSession());
        String oldSessionId = csrf.session().getId();

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .session(csrf.session()).header(csrf.headerName(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistration())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Tester"))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();

        long userId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        assertEquals(1, userRepository.count());
        assertEquals(1, credentialsRepository.count());
        String hash = credentialsRepository.findById(userId).orElseThrow().getPasswordHash();
        assertNotEquals(PASSWORD, hash);
        assertTrue(passwordEncoder.matches(PASSWORD, hash));
        assertNotEquals(oldSessionId, csrf.session().getId());
        assertCurrentUser(csrf.session(), userId);
        assertOldCsrfRejectedAndFreshTokenAccepted(csrf);
    }

    @Test
    void shouldReturn409WhenEmailIsAlreadyRegistered() throws Exception {
        UserResponse existing = authService.register("Tester", EMAIL, PASSWORD);
        CsrfSession csrf = getCsrf(new MockHttpSession());

        mockMvc.perform(post("/auth/register")
                        .session(csrf.session()).header(csrf.headerName(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistration())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        assertEquals(1, userRepository.count());
        assertEquals(1, credentialsRepository.count());
        assertEquals("Tester", userRepository.findById(existing.id()).orElseThrow().getName());
        mockMvc.perform(get("/auth/me").session(csrf.session()))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("invalidRegistrations")
    void shouldReturn400ForInvalidRegistration(RegisterRequest request) throws Exception {
        CsrfSession csrf = getCsrf(new MockHttpSession());

        mockMvc.perform(post("/auth/register")
                        .session(csrf.session()).header(csrf.headerName(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertEquals(0, userRepository.count());
        assertEquals(0, credentialsRepository.count());
    }

    static Stream<RegisterRequest> invalidRegistrations() {
        return Stream.of(
                new RegisterRequest("", EMAIL, PASSWORD),
                new RegisterRequest("ab", EMAIL, PASSWORD),
                new RegisterRequest("a".repeat(51), EMAIL, PASSWORD),
                new RegisterRequest("Tester", "", PASSWORD),
                new RegisterRequest("Tester", "invalid-email", PASSWORD),
                new RegisterRequest("Tester", "a".repeat(40) + "@example.com", PASSWORD),
                new RegisterRequest("Tester", EMAIL, ""),
                new RegisterRequest("Tester", EMAIL, "a".repeat(16)),
                new RegisterRequest("Tester", EMAIL, "a".repeat(129))
        );
    }

    @Test
    void shouldLoginPersistAuthenticationAndRotateSessionId() throws Exception {
        UserResponse registered = authService.register("Tester", EMAIL, PASSWORD);
        CsrfSession csrf = getCsrf(new MockHttpSession());
        String oldSessionId = csrf.session().getId();

        mockMvc.perform(post("/auth/login")
                        .session(csrf.session()).header(csrf.headerName(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(EMAIL, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(registered.id()))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        assertNotEquals(oldSessionId, csrf.session().getId());
        assertCurrentUser(csrf.session(), registered.id());
        assertOldCsrfRejectedAndFreshTokenAccepted(csrf);
    }

    @ParameterizedTest
    @ValueSource(strings = {"tester@example.com", "unknown@example.com"})
    void shouldReturnGeneric401ForInvalidCredentials(String email) throws Exception {
        authService.register("Tester", EMAIL, PASSWORD);
        CsrfSession csrf = getCsrf(new MockHttpSession());

        mockMvc.perform(post("/auth/login")
                        .session(csrf.session()).header(csrf.headerName(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        mockMvc.perform(get("/auth/me").session(csrf.session()))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("invalidLogins")
    void shouldReturn400ForInvalidLogin(LoginRequest request) throws Exception {
        CsrfSession csrf = getCsrf(new MockHttpSession());

        mockMvc.perform(post("/auth/login")
                        .session(csrf.session()).header(csrf.headerName(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    static Stream<LoginRequest> invalidLogins() {
        return Stream.of(
                new LoginRequest("", PASSWORD),
                new LoginRequest("invalid-email", PASSWORD),
                new LoginRequest(EMAIL, "")
        );
    }

    @Test
    void shouldReturn401WhenCurrentUserIsRequestedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn404WhenSessionUserNoLongerExists() throws Exception {
        UserResponse registered = authService.register("Tester", EMAIL, PASSWORD);
        MockHttpSession session = login(EMAIL);
        credentialsRepository.deleteById(registered.id());
        userRepository.deleteById(registered.id());

        mockMvc.perform(get("/auth/me").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldKeepAuthenticatedSessionsSeparate() throws Exception {
        UserResponse first = authService.register("Tester", EMAIL, PASSWORD);
        UserResponse second = authService.register("Other", "other@example.com", PASSWORD);
        MockHttpSession firstSession = login(EMAIL);
        MockHttpSession secondSession = login("other@example.com");

        assertCurrentUser(firstSession, first.id());
        assertCurrentUser(secondSession, second.id());
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLogoutInvalidateSessionAndDeleteCookie() throws Exception {
        UserResponse registered = authService.register("Tester", EMAIL, PASSWORD);
        MockHttpSession session = login(EMAIL);
        assertCurrentUser(session, registered.id());
        CsrfSession csrf = getCsrf(session);

        mockMvc.perform(post("/auth/logout")
                        .session(session).header(csrf.headerName(), csrf.token()))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("JSESSIONID", 0));

        assertTrue(session.isInvalid());
        mockMvc.perform(get("/auth/me").session(session))
                .andExpect(status().isUnauthorized());
        assertTrue(userRepository.existsById(registered.id()));
        assertEquals(1, credentialsRepository.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/auth/register", "/auth/login", "/auth/logout"})
    void shouldRejectMissingCsrfToken(String endpoint) throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistration())))
                .andExpect(status().isForbidden());

        assertEquals(0, userRepository.count());
        assertEquals(0, credentialsRepository.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/auth/register", "/auth/login", "/auth/logout"})
    void shouldRejectInvalidCsrfToken(String endpoint) throws Exception {
        CsrfSession csrf = getCsrf(new MockHttpSession());

        mockMvc.perform(post(endpoint)
                        .session(csrf.session()).header(csrf.headerName(), "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistration())))
                .andExpect(status().isForbidden());

        assertEquals(0, userRepository.count());
        assertEquals(0, credentialsRepository.count());
    }

    @Test
    void shouldKeepSessionAuthenticatedWhenLogoutHasNoCsrfToken() throws Exception {
        UserResponse registered = authService.register("Tester", EMAIL, PASSWORD);
        MockHttpSession session = login(EMAIL);

        mockMvc.perform(post("/auth/logout").session(session))
                .andExpect(status().isForbidden());

        assertFalse(session.isInvalid());
        assertCurrentUser(session, registered.id());
    }

    @Test
    void shouldRejectCsrfTokenFromAnotherSession() throws Exception {
        CsrfSession first = getCsrf(new MockHttpSession());
        CsrfSession second = getCsrf(new MockHttpSession());

        mockMvc.perform(post("/auth/register")
                        .session(second.session()).header(first.headerName(), first.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistration())))
                .andExpect(status().isForbidden());

        assertEquals(0, userRepository.count());
        assertEquals(0, credentialsRepository.count());
    }

    private RegisterRequest validRegistration() {
        return new RegisterRequest("Tester", EMAIL, PASSWORD);
    }

    private MockHttpSession login(String email) throws Exception {
        CsrfSession csrf = getCsrf(new MockHttpSession());
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .session(csrf.session()).header(csrf.headerName(), csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private void assertCurrentUser(MockHttpSession session, long userId) throws Exception {
        mockMvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    private void assertOldCsrfRejectedAndFreshTokenAccepted(CsrfSession oldCsrf) throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .session(oldCsrf.session()).header(oldCsrf.headerName(), oldCsrf.token()))
                .andExpect(status().isForbidden());
        assertFalse(oldCsrf.session().isInvalid());

        CsrfSession freshCsrf = getCsrf(oldCsrf.session());
        mockMvc.perform(post("/auth/logout")
                        .session(freshCsrf.session()).header(freshCsrf.headerName(), freshCsrf.token()))
                .andExpect(status().isNoContent());
        assertTrue(freshCsrf.session().isInvalid());
    }

    private CsrfSession getCsrf(MockHttpSession session) throws Exception {
        MvcResult result = mockMvc.perform(get("/auth/csrf").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.headerName").isNotEmpty())
                .andExpect(jsonPath("$.parameterName").isNotEmpty())
                .andReturn();
        var body = objectMapper.readTree(result.getResponse().getContentAsString());
        return new CsrfSession((MockHttpSession) result.getRequest().getSession(false),
                body.get("headerName").asText(), body.get("parameterName").asText(), body.get("token").asText());
    }

    private record CsrfSession(MockHttpSession session, String headerName, String parameterName, String token) {
    }
}
