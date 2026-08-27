package com.hamooda.hotelmcp.auth;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.List;
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

    public List<Map<String, Object>> getList(String uri) {

        try {
            return executeGetList(uri);
        } catch (RestClientResponseException ex) {

            if (ex.getStatusCode().value() != 401) {
                throw ex;
            }

            if (!tokenManager.refreshAccessToken()) {
                throw ex;
            }

            return executeGetList(uri);
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

    public Map<String, Object> uploadImageFromUrl(
            String endpoint,
            String imageUrl,
            String fileName) {

        URI source = URI.create(imageUrl);
        if (!"https".equalsIgnoreCase(source.getScheme())) {
            throw new IllegalArgumentException("Image URL must use HTTPS");
        }

        byte[] image = RestClient.create()
                .get()
                .uri(source)
                .retrieve()
                .body(byte[].class);

        if (image == null || image.length == 0) {
            throw new IllegalArgumentException("Image URL returned no content");
        }

        if (image.length > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Image must be 5 MB or smaller");
        }

        String safeFileName = (fileName == null || fileName.isBlank())
                ? "image.jpg"
                : fileName;

        ByteArrayResource resource = new ByteArrayResource(image) {
            @Override
            public String getFilename() {
                return safeFileName;
            }
        };

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", resource);

        try {
            return executeMultipart(endpoint, parts);
        } catch (RestClientResponseException ex) {

            if (ex.getStatusCode().value() != 401) {
                throw ex;
            }

            if (!tokenManager.refreshAccessToken()) {
                throw ex;
            }

            return executeMultipart(endpoint, parts);
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

    private List<Map<String, Object>> executeGetList(String uri) {

        return restClient.get()
                .uri(uri)
                .headers(headers ->
                        headers.setBearerAuth(tokenManager.getAccessToken()))
                .retrieve()
                .body(List.class);
    }

    private Map<String, Object> executePatch(String uri) {

        return restClient.patch()
                .uri(uri)
                .headers(headers ->
                        headers.setBearerAuth(tokenManager.getAccessToken()))
                .retrieve()
                .body(Map.class);
    }

    private Map<String, Object> executeMultipart(
            String uri,
            MultiValueMap<String, Object> parts) {

        return restClient.post()
                .uri(uri)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .headers(headers ->
                        headers.setBearerAuth(tokenManager.getAccessToken()))
                .body(parts)
                .retrieve()
                .body(Map.class);
    }
}