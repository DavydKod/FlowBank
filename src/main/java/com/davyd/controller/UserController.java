package com.davyd.controller;

import com.davyd.dto.request.ChangeUserNameRequest;
import com.davyd.dto.request.CreateUserRequest;
import com.davyd.dto.response.UserResponse;
import com.davyd.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

@Tag(
        name = "Users",
        description = "User management"
)
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @Operation(
            summary = "Get all users",
            description = "Returns a paginated list of users"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully"
            )
    })
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @ParameterObject
            @PageableDefault(size = 20)
            Pageable pageable){
        Page<UserResponse> userResponses = userService.getAllUsers(pageable);
        return ResponseEntity.ok(userResponses);
    }

    @Operation(
            summary = "Get user by ID",
            description = "Returns a user associated with the specified ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user ID"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "User ID", example = "1")
            @PathVariable
            @Positive
            long id){
        UserResponse userResponse = userService.getUser(id);
        return ResponseEntity.ok(userResponse);
    }

    @Operation(
            summary = "Get user by email",
            description = "Returns a user associated with the specified email address"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid email address"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> getUserByEmail(
            @Parameter(description = "User email address", example = "adri@gmail.com")
            @RequestParam
            @Email
            String email
    ) {
        UserResponse userResponse = userService.getUser(email);
        return ResponseEntity.ok(userResponse);
    }

    @Operation(
            summary = "Create user",
            description = "Creates a new user with the provided name and email address"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user data"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A user with this email already exists"
            )
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestBody
            @Valid CreateUserRequest createUserRequest){
        UserResponse userResponse = userService.createUser(createUserRequest.name(), createUserRequest.email());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userResponse);
    }

    @Operation(
            summary = "Change user name",
            description = "Updates the name of the specified user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User name updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user ID or name"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    @PatchMapping("/{id}/name")
    public ResponseEntity<UserResponse> changeUserName(
            @Parameter(description = "User ID", example = "1")
            @PathVariable
            @Positive
            long id,

            @RequestBody
            @Valid
            ChangeUserNameRequest request
    ) {
        UserResponse userResponse = userService.changeUserName(id, request.name());
        return ResponseEntity.ok(userResponse);
    }

    @Operation(
            summary = "Delete user",
            description = "Deletes the specified user if deletion is allowed"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user ID"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User cannot be deleted because his bank accounts exist"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User Id", example = "1")
            @PathVariable
            @Positive
            long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


}
