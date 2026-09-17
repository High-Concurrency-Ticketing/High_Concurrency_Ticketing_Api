package com.highconcurrency.ticketing.presentation;

import com.highconcurrency.ticketing.application.common.ErrorCode;
import com.highconcurrency.ticketing.application.common.HighConcurrencyTicketingException;
import com.highconcurrency.ticketing.application.usecase.auth.AuthToken;
import com.highconcurrency.ticketing.application.usecase.auth.AuthUseCase;
import com.highconcurrency.ticketing.application.usecase.auth.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final long REFRESH_TOKEN_MAX_AGE_SECONDS = 7 * 24 * 60 * 60;

    private final AuthUseCase authUseCase;

    @Value("${auth.cookie.secure}")
    private boolean secureCookie;

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        AuthToken authToken = authUseCase.login(request);
        return tokenResponse(authToken);
    }

    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급")
    public ResponseEntity<String> reissue(@CookieValue(value = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new HighConcurrencyTicketingException(ErrorCode.UNAUTHORIZED, "리프레시 토큰이 없습니다.");
        }

        AuthToken authToken = authUseCase.reissue(refreshToken);
        return tokenResponse(authToken);
    }

    private ResponseEntity<String> tokenResponse(AuthToken authToken) {
        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, authToken.refreshToken())
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(secureCookie ? "None" : "Lax")
                .path("/v1/auth")
                .maxAge(REFRESH_TOKEN_MAX_AGE_SECONDS)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(authToken.accessToken());
    }
}
