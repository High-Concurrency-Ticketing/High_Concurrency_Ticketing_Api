package com.highconcurrency.ticketing.infrastructure.jwt;

import com.highconcurrency.ticketing.application.port.TokenPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

@Component
public class JwtTokenAdapter implements TokenPort {

    private static final String TOKEN_TYPE = "tokenType";
    private static final String ACCESS_TOKEN = "ACCESS";
    private static final String REFRESH_TOKEN = "REFRESH";
    private static final Duration ACCESS_TOKEN_EXPIRATION = Duration.ofMinutes(15);
    private static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(7);

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtTokenAdapter(@Value("${auth.jwt.secret}") String secret) {
        SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.jwtEncoder = NimbusJwtEncoder.withSecretKey(secretKey).build();
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @Override
    public String createAccessToken(Long userId, String email) {
        return createToken(userId, email, ACCESS_TOKEN, ACCESS_TOKEN_EXPIRATION);
    }

    @Override
    public String createRefreshToken(Long userId, String email) {
        return createToken(userId, email, REFRESH_TOKEN, REFRESH_TOKEN_EXPIRATION);
    }

    @Override
    public Long getUserId(String token) {
        return Long.valueOf(jwtDecoder.decode(token).getSubject());
    }

    @Override
    public boolean isRefreshToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return REFRESH_TOKEN.equals(jwt.getClaimAsString(TOKEN_TYPE));
    }

    private String createToken(Long userId, String email, String tokenType, Duration expiration) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim(TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
