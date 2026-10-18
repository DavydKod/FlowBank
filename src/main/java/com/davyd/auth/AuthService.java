package com.davyd.auth;

import com.davyd.user.User;
import com.davyd.user.UserService;
import com.davyd.user.dto.response.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserCredentialsRepository userCredentialsRepository;
    private final UserService userService;

    public AuthService(PasswordEncoder passwordEncoder,
                       UserCredentialsRepository userCredentialsRepository,
                       UserService userService){
        this.passwordEncoder = passwordEncoder;
        this.userCredentialsRepository = userCredentialsRepository;
        this.userService = userService;
    }

    @Transactional
    public UserResponse register(String name, String email, String password){
        String passwordHash = passwordEncoder.encode(password);

        UserResponse userResponse = userService.createUser(name, email);

        User user = userService.getUserEntity(userResponse.id());

        UserCredentials userCredentials = new UserCredentials(user, passwordHash);

        userCredentialsRepository.save(userCredentials);

        return userResponse;
    }
}
