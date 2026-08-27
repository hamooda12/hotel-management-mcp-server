package com.hamooda.hotelmcp.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Component
public class AuthenticatedRestClient {

    private final RestClient restClient;
    private final TokenManager tokenManager;

    public AuthenticatedRestClient(
            RestClient restClient,
            TokenManager tokenManager) {

        this.restClient = restClient;
        this.tokenManager = tokenManager;
    }

    public Map<String, Object> post(
            String uri,
            Map<String, Object> body) {

        try {
            return executePost(uri, body);
        } catch (RestClientResponseException ex) {

            if (ex.getStatusCode().value() != 401) {
                throw ex;
            }

            if (!tokenManager.refreshAccessToken()) {
                throw ex;
            }

            return executePost(uri, body);
        }
    }

    public Map<String, Object> get(String uri) {

        try {
            return executeGet(uri);
        } catch (RestClientResponseException ex) {

            if (ex.getStatusCode().value() != 401) {
                throw ex;
            }

            if (!tokenManager.refreshAccessToken()) {
                throw ex;
            }

            return executeGet(uri);
        }
    }

    public Map<String, Object> patch(String uri) {

        try {
            return executePatch(uri);
        } catch (RestClientResponseException ex) {

            if (ex.getStatusCode().value() != 401) {
                throw ex;
            }

            if (!tokenManager.refreshAccessToken()) {
                throw ex;
            }

            return executePatch(uri);
        }
    }

    private Map<String, Object> executePost(
            String uri,
            Map<String, Object> body) {

        return restClient.post()
                .uri(uri)
                .headers(headers ->
                        headers.setBearerAuth(tokenManager.getAccessToken()))
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    private Map<String, Object> executeGet(String uri) {

        return restClient.get()
                .uri(uri)
                .headers(headers ->
                        headers.setBearerAuth(tokenManager.getAccessToken()))
                .retrieve()
                .body(Map.class);
    }

    private Map<String, Object> executePatch(String uri) {

        return restClient.patch()
                .uri(uri)
                .headers(headers ->
                        headers.setBearerAuth(tokenManager.getAccessToken()))
                .retrieve()
                .body(Map.class);
    }
}