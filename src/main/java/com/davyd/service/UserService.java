package com.davyd.service;

import com.davyd.exception.UserNotFoundException;
import com.davyd.models.User;
import com.davyd.repository.UserRepository;

import java.util.List;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User getUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(
                        () -> new UserNotFoundException(id)
                );
    }

    public User getUser(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(String name, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException(
                    "User with email " + email + " already exists"
            );
        }

        User user = new User(name, email);
        return userRepository.save(user);
    }

    public User changeUserName(long id, String newName) {
        User user = getUser(id);

        user.changeName(newName);

        return userRepository.save(user);
    }

    public void deleteUser(long id) {
        User user = getUser(id);
        userRepository.delete(user);
    }

    public boolean existsById(long id) {
        return userRepository.existsById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
