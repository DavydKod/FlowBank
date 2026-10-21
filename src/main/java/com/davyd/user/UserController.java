package com.davyd.user;

import com.davyd.security.principal.FlowBankPrincipal;
import com.davyd.common.exception.dto.ErrorResponse;
import com.davyd.user.dto.request.ChangeUserNameRequest;
import com.davyd.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;


@Tag(
        name = "Users",
        description = "Current user profile management using session authentication"
)
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

    public UserController(UserService userService){
        this.userService = userService;
    }

    @Operation(
            summary = "Change the current user's name",
            description = "Updates the name of the user authenticated by the session cookie. "
                    + "Requires a valid CSRF token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User name updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or name",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Missing or invalid CSRF token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Current user no longer exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/me/name")
    public ResponseEntity<UserResponse> changeUserName(
            @RequestBody
            @Valid
            ChangeUserNameRequest request,

            @Parameter(hidden = true) @AuthenticationPrincipal FlowBankPrincipal principal
    ) {
        UserResponse userResponse = userService.changeUserName(principal.getUserId(), request.name());
        return ResponseEntity.ok(userResponse);
    }

    @Operation(
            summary = "Delete the current user",
            description = "Deletes the user authenticated by the session cookie if they have no bank accounts. "
                    + "Requires a valid CSRF token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Missing or invalid CSRF token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Current user no longer exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User still has bank accounts or a data integrity constraint prevents deletion",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(
            @Parameter(hidden = true) @AuthenticationPrincipal FlowBankPrincipal principal,
            HttpServletRequest request,
            HttpServletResponse response,
            @Parameter(hidden = true) Authentication authentication) {
        userService.deleteUser(principal.getUserId());

        logoutHandler.logout(request, response, authentication);

        return ResponseEntity.noContent().build();
    }
}
