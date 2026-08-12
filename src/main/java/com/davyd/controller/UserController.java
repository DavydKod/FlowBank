package com.davyd.controller;

import com.davyd.dto.ChangeUserNameRequest;
import com.davyd.dto.CreateUserRequest;
import com.davyd.models.User;
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
    public ResponseEntity<List<User>> getAllUser(){
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable @Positive long id){
        User user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/by-email")
    public ResponseEntity<User> getUserByEmail(
            @RequestParam @Email String email
    ) {
        User user = userService.getUser(email);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid CreateUserRequest createUserRequest){
        User user = userService.createUser(createUserRequest.name(), createUserRequest.email());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<User> changeUserName(
            @PathVariable @Positive long id,
            @RequestBody @Valid ChangeUserNameRequest request
    ) {
        User user = userService.changeUserName(id, request.name());
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable @Positive long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


}
