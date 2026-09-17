package com.highconcurrency.ticketing.application.usecase.user;

public record UserUpdateRequest(
        String name,
        String password
) {
}
