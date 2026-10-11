package com.davyd.user.exception;

import com.davyd.common.exception.NotFoundException;
import com.davyd.common.util.Validation;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(long userId){
        super("User with ID " + userId + " not found");
    }

    public UserNotFoundException(String email){
        super("User with email " + Validation.validateEmail(email) + " not found");
    }
}
