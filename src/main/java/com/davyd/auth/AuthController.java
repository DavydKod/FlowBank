package com.davyd.auth;

import com.davyd.auth.dto.request.LoginRequest;
import com.davyd.auth.dto.request.RegisterRequest;
import com.davyd.common.exception.dto.ErrorResponse;
import com.davyd.security.principal.FlowBankPrincipal;
import com.davyd.user.UserService;
import com.davyd.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Tag(
        name = "Registration",
        description = "User Registration"
)
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final SecurityContextRepository securityContextRepository;
    private final UserService userService;

    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager,
            SessionAuthenticationStrategy sessionAuthenticationStrategy,
            SecurityContextRepository securityContextRepository,
            UserService userService
    ) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.securityContextRepository = securityContextRepository;
        this.userService = userService;
    }

    @GetMapping("/csrf")
    public ResponseEntity<CsrfToken> csrf(CsrfToken csrfToken){
        return ResponseEntity.ok(csrfToken);
    }

    @Operation(
            summary = "Register a user",
            description = "Creates a user and associated credentials with a hashed password. "
                    + "Returns the user profile without the password or password hash.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User registered successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid registration data",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required by the current security configuration",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied, including a missing or invalid CSRF token",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Email already exists or a data integrity constraint is violated",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestBody
            @Valid
            RegisterRequest registerRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        UserResponse userResponse = authService.register(registerRequest.name(), registerRequest.email(),
                registerRequest.password());

        authenticateAndSave(registerRequest.email(), registerRequest.password(), request, response);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @RequestBody
            @Valid
            LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        Authentication authentication = authenticateAndSave(
                loginRequest.email(), loginRequest.password(), request, response
        );
        return ResponseEntity.ok(userService.getUser(authentication.getName()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal FlowBankPrincipal principal) {
        return ResponseEntity.ok(userService.getUser(principal.getUserId()));
    }

    private Authentication authenticateAndSave(
            String email,
            String password,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        email,
                        password
                )
        );

        sessionAuthenticationStrategy.onAuthentication(
                authentication, request, response
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);

        return authentication;
    }
}
