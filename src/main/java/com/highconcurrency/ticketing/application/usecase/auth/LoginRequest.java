package com.highconcurrency.ticketing.application.usecase.auth;

public record LoginRequest(
        String email,
        String password
) {
}
