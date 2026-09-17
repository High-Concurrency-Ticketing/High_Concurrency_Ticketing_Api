package com.highconcurrency.ticketing.application.usecase.auth;

import com.highconcurrency.ticketing.application.common.ErrorCode;
import com.highconcurrency.ticketing.application.common.HighConcurrencyTicketingException;
import com.highconcurrency.ticketing.application.port.TokenPort;
import com.highconcurrency.ticketing.application.usecase.user.UserUseCase;
import com.highconcurrency.ticketing.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserUseCase userUseCase;
    private final PasswordEncoder passwordEncoder;
    private final TokenPort tokenPort;

    @Override
    @Transactional
    public AuthToken login(LoginRequest request) {
        User user = userUseCase.getUser(request.email());

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new HighConcurrencyTicketingException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return issueToken(user);
    }

    @Override
    @Transactional
    public AuthToken reissue(String refreshToken) {
        if (!tokenPort.isRefreshToken(refreshToken)) {
            throw new HighConcurrencyTicketingException(ErrorCode.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = tokenPort.getUserId(refreshToken);

        User user = userUseCase.getUser(userId);
        user.equalsRefreshToken(refreshToken);

        return issueToken(user);
    }

    private AuthToken issueToken(User user) {
        String accessToken = tokenPort.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = tokenPort.createRefreshToken(user.getId(), user.getEmail());

        user.updateRefreshToken(refreshToken);

        return new AuthToken(accessToken, refreshToken);
    }
}
