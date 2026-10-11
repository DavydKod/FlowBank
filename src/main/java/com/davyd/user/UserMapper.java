package com.davyd.user;

import com.davyd.user.dto.response.UserResponse;

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
