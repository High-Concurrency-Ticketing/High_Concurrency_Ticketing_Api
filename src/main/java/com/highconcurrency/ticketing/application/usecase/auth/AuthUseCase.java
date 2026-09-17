package com.highconcurrency.ticketing.application.usecase.auth;

public interface AuthUseCase {

    AuthToken login(LoginRequest request);

    AuthToken reissue(String refreshToken);
}
