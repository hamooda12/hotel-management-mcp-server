package com.hamooda.hotelmcp.auth;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class RegisterUserTool {

    private final RestClient hotelBackendRestClient;

    public RegisterUserTool(RestClient hotelBackendRestClient) {
        this.hotelBackendRestClient = hotelBackendRestClient;
    }

    @Tool(description = "Register a new user account in the Hotel Management backend. New users are GUESTs by default. Use only GUEST unless an administrator explicitly requires another role.")
    public Map<String, Object> registerUser(
            @ToolParam(description = "Email address for the new user.") String email,
            @ToolParam(description = "Password for the new user account.") String password) {

        Map<String, Object> request = new HashMap<>();
        request.put("email", email);
        request.put("password", password);
        request.put("role", "GUEST");

        hotelBackendRestClient.post()
                .uri("/api/auth/register")
                .body(request)
                .retrieve()
                .toBodilessEntity();

        return Map.of(
                "success", true,
                "message", "User registered successfully",
                "email", email,
                "role", "GUEST"
        );
    }
}
