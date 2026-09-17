package com.highconcurrency.ticketing.application.port;

public interface TokenPort {

    String createAccessToken(Long userId, String email);

    String createRefreshToken(Long userId, String email);

    Long getUserId(String token);

    boolean isRefreshToken(String token);
}
