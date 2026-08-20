package com.davyd.mapper;

import com.davyd.dto.response.UserResponse;
import com.davyd.models.User;

public class UserMapper {
    private UserMapper(){}

    public static UserResponse toResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
