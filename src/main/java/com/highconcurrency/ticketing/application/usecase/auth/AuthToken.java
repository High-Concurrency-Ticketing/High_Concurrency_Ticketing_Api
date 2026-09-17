package com.highconcurrency.ticketing.application.usecase.auth;

public record AuthToken(
        String accessToken,
        String refreshToken
) {
}
