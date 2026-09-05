package com.hamooda.hotelmcp.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Component
public class TokenManager {
    private static final String BEARER_PREFIX = "Bearer ";

    private final RestClient authRestClient;
    private String accessToken;
    private String refreshToken;

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

        if (!(newAccessToken instanceof String newToken) || newToken.isBlank()) {
            return false;
        }

        if (!(newRefreshToken instanceof String newRefreshTokenValue) || newRefreshTokenValue.isBlank()) {
            return false;
        }

        updateAccessToken(newToken);
        updateRefreshToken(newRefreshTokenValue);

        return true;
    }

    public void updateRefreshToken(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
    }

    public void updateAccessToken(String newAccessToken) {
        this.accessToken = newAccessToken;
    }

    /**
     * Returns the JWT belonging to the current HTTP request when the MCP server
     * was called on behalf of a logged-in frontend user. Falls back to the
     * configured server token for non-user/background calls.
     */
    public String getAccessToken() {
        String requestToken = getRequestAccessToken();
        return requestToken != null ? requestToken : accessToken;
    }

    public boolean hasAccessToken() {
        return getAccessToken() != null && !getAccessToken().isBlank();
    }

    public boolean hasRefreshToken() {
        return refreshToken != null && !refreshToken.isBlank();
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * True when the current MCP request carries a user JWT. Such a token must
     * never be replaced by the MCP server's refresh-token flow.
     */
    public boolean hasRequestAccessToken() {
        return getRequestAccessToken() != null;
    }

    private String getRequestAccessToken() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }

        HttpServletRequest request = servletAttributes.getRequest();
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        return token.isBlank() ? null : token;
    }
}
