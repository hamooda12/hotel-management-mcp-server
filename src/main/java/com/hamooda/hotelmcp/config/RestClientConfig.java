package com.hamooda.hotelmcp.config;

import com.hamooda.hotelmcp.auth.TokenManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient hotelBackendRestClient(
            @Value("${hotel-management.backend.base-url}") String baseUrl,
            TokenManager tokenManager) {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {

                    if (tokenManager.hasAccessToken()) {
                        request.getHeaders().setBearerAuth(
                                tokenManager.getAccessToken()
                        );
                    }

                    return execution.execute(request, body);
                })
                .build();
    }
}