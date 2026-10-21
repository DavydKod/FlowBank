package com.davyd.unit.service;

import com.davyd.auth.AuthController;
import com.davyd.auth.AuthService;
import com.davyd.security.config.SecurityConfig;
import com.davyd.user.UserService;
import com.davyd.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.davyd.security.principal.FlowBankPrincipal;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthSessionTest {
    @Autowired MockMvc mvc;
    @MockitoBean AuthService authService;
    @MockitoBean UserService userService;
    @MockitoBean AuthenticationManager authenticationManager;

    private void successfulAuthentication() {
        FlowBankPrincipal principal = new FlowBankPrincipal(
                42L,
                "test@example.com",
                "unused"
        );

        when(authenticationManager.authenticate(any())).thenReturn(
                UsernamePasswordAuthenticationToken.authenticated(
                        principal,
                        null,
                        principal.getAuthorities()
                )
        );

        UserResponse response = new UserResponse(
                42L,
                "Tester",
                "test@example.com"
        );

        when(userService.getUser("test@example.com"))
                .thenReturn(response);

        when(userService.getUser(42L))
                .thenReturn(response);
    }

    @Test
    void loginPersistsSessionRotatesIdAndLogoutInvalidatesIt() throws Exception {
        successfulAuthentication();
        MockHttpSession session = new MockHttpSession();
        String oldId = session.getId();
        mvc.perform(post("/auth/login").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("id").value(42));
        assertNotEquals(oldId, session.getId());
        mvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("id").value(42));
        mvc.perform(post("/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent()).andExpect(cookie().maxAge("JSESSIONID", 0));
        assertTrue(session.isInvalid());
        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void registrationAutomaticallyLogsIn() throws Exception {
        successfulAuthentication();
        when(authService.register(any(), any(), any()))
                .thenReturn(new UserResponse(42L, "Tester", "test@example.com"));
        var result = mvc.perform(post("/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tester\",\"email\":\"test@example.com\",\"password\":\"long-enough-password\"}"))
                .andExpect(status().isCreated()).andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        mvc.perform(get("/auth/me").session(session)).andExpect(status().isOk());
    }

    @Test
    void invalidCredentialsReturnGeneric401() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("internal detail"));
        mvc.perform(post("/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("message").value("Invalid email or password"));
    }

    @Test
    void csrfIsPublicAndRequiredForLoginAndLogout() throws Exception {
        mvc.perform(get("/auth/csrf")).andExpect(status().isOk())
                .andExpect(jsonPath("token").isNotEmpty());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/auth/logout")).andExpect(status().isForbidden());
    }
}
