package com.highconcurrency.ticketing.application.usecase.user;

import com.highconcurrency.ticketing.domain.user.User;

public record UserResponse(
        Long id,
        String email,
        String name
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }
}
