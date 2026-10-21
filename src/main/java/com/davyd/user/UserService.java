package com.davyd.user;

import com.davyd.user.dto.response.UserResponse;
import com.davyd.user.exception.EmailAlreadyExistsException;
import com.davyd.user.exception.UserDeletionNotAllowedException;
import com.davyd.user.exception.UserNotFoundException;
import com.davyd.bankaccount.BankAccountRepository;
import com.davyd.common.util.Validation;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    public UserService(UserRepository userRepository, BankAccountRepository bankAccountRepository){
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    public UserResponse getUser(long id) {

        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new UserNotFoundException(id)
                );

        return UserMapper.toResponse(user);
    }

    public User getUserEntity(long id) {
        return userRepository.findById(id)
                .orElseThrow(
                        () -> new UserNotFoundException(id)
                );
    }

    public UserResponse getUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException(email));


        return UserMapper.toResponse(user);
    }

    @Transactional
    public UserResponse createUser(String name, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        try {
            return UserMapper.toResponse(userRepository.saveAndFlush(new User(name, email)));
        } catch (DataIntegrityViolationException e){
            if (Validation.isConstraintViolation(e, "uk_users_email")){
                throw new EmailAlreadyExistsException(email);
            }

            throw e;
        }
    }

    @Transactional
    public UserResponse changeUserName(long currentUserId, String newName) {
        User user = getUserEntity(currentUserId);

        user.changeName(newName);

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(long currentUserId) {
        User user = getUserEntity(currentUserId);

        if (bankAccountRepository.existsByOwnerId(currentUserId)){
            throw new UserDeletionNotAllowedException("Impossible to delete user with bankAccount");
        }

        try {
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException e){
            if (Validation.isConstraintViolation(e, "fk_bank_accounts_owner")){
                throw new UserDeletionNotAllowedException("Impossible to delete user with bankAccount");
            }

            throw e;
        }
    }
}
