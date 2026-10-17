package com.davyd.auth;

import com.davyd.auth.dto.request.RegisterRequest;
import com.davyd.common.exception.dto.ErrorResponse;
import com.davyd.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Registration",
        description = "User Registration"
)
@RestController
@RequestMapping("/registration")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
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
    @PostMapping
    public ResponseEntity<UserResponse> register(
            @RequestBody
            @Valid
            RegisterRequest registerRequest
    ){
        UserResponse userResponse = authService.register(registerRequest.name(), registerRequest.email(),
                registerRequest.password());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userResponse);
    }
}
