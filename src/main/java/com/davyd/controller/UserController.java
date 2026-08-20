package com.davyd.controller;

import com.davyd.dto.request.ChangeUserNameRequest;
import com.davyd.dto.request.CreateUserRequest;
import com.davyd.dto.response.UserResponse;
import com.davyd.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUser(){
        List<UserResponse> userResponses = userService.getAllUsers();
        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable @Positive long id){
        UserResponse userResponse = userService.getUser(id);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> getUserByEmail(
            @RequestParam @Email String email
    ) {
        UserResponse userResponse = userService.getUser(email);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest createUserRequest){
        UserResponse userResponse = userService.createUser(createUserRequest.name(), createUserRequest.email());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userResponse);
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<UserResponse> changeUserName(
            @PathVariable @Positive long id,
            @RequestBody @Valid ChangeUserNameRequest request
    ) {
        UserResponse userResponse = userService.changeUserName(id, request.name());
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable @Positive long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


}
