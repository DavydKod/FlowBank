package com.davyd.service;

import com.davyd.dto.response.UserResponse;
import com.davyd.exception.UserNotFoundException;
import com.davyd.mapper.UserMapper;
import com.davyd.models.User;
import com.davyd.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public UserResponse getUser(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new UserNotFoundException(id)
                );

        return UserMapper.toResponse(user);
    }

    private User getUserEntity(long id) {
        return userRepository.findById(id)
                .orElseThrow(
                        () -> new UserNotFoundException(id)
                );
    }

    public UserResponse getUser(String email){
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException(email));


        return UserMapper.toResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream().map(UserMapper::toResponse).toList();
    }

    public UserResponse createUser(String name, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException(
                    "User with email " + email + " already exists"
            );
        }

        User user = new User(name, email);

        return UserMapper.toResponse(userRepository.save(user));
    }

    public UserResponse changeUserName(long id, String newName) {
        User user = getUserEntity(id);

        user.changeName(newName);

        return UserMapper.toResponse(userRepository.save(user));
    }

    public void deleteUser(long id) {
        User user = getUserEntity(id);
        userRepository.delete(user);
    }

    public boolean existsById(long id) {
        return userRepository.existsById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
