package org.example.exception;

import org.example.util.Validation;

public class UserNotFoundException extends NotFoundException{
    public UserNotFoundException(long userId){
        super("User with ID " + userId + " not found");
    }

    public UserNotFoundException(String email){
        super("User with email " + Validation.validateEmail(email) + " not found");
    }
}
