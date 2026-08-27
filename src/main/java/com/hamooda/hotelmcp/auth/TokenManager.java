package com.hamooda.hotelmcp.auth;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

import java.util.Map;
@Component
public class TokenManager {
    private final RestClient authRestClient;
    private String accessToken;
    private  String refreshToken;

    public TokenManager(
            @Value("${hotel-management.backend.base-url}") String baseUrl) {

        this.accessToken = System.getenv("HOTEL_BACKEND_ACCESS_TOKEN");
        this.refreshToken = System.getenv("HOTEL_BACKEND_REFRESH_TOKEN");

        this.authRestClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
    public synchronized boolean refreshAccessToken() {

        if (!hasRefreshToken()) {
            return false;
        }

        Map<String, String> request = Map.of(
                "refreshToken", refreshToken
        );

        Map response = authRestClient.post()
                .uri("/api/auth/refresh")
                .body(request)
                .retrieve()
                .body(Map.class);

        Object newAccessToken = response.get("accessToken");
        Object newRefreshToken = response.get("refreshToken");

        if (!(newAccessToken instanceof String accessToken) || accessToken.isBlank()) {
            return false;
        }

        if (!(newRefreshToken instanceof String refreshToken) || refreshToken.isBlank()) {
            return false;
        }

        updateAccessToken(accessToken);
        updateRefreshToken(refreshToken);

        return true;
    }
    public void updateRefreshToken(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
    }

    public void updateAccessToken(String newAccessToken) {
        this.accessToken = newAccessToken;
    }

    public boolean hasAccessToken() {
        return accessToken != null && !accessToken.isBlank();
    }

    public boolean hasRefreshToken() {
        return refreshToken != null && !refreshToken.isBlank();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}