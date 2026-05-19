package com.medicology.learning.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicology.learning.client.dto.AssessmentUserProgressSnapshot;
import com.medicology.learning.dto.common.ApiResponse;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class AssessmentProgressClient {

    private static final String TOKEN_HEADER = "X-Internal-Service-Token";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String assessmentBaseUrl;
    private final String internalServiceToken;

    public AssessmentProgressClient(
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper,
            @Value("${assessment.service-url:http://localhost:8083}") String assessmentBaseUrl,
            @Value("${app.internal-service-token:}") String internalServiceToken) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
        this.assessmentBaseUrl = trimTrailingSlash(assessmentBaseUrl);
        this.internalServiceToken = internalServiceToken;
    }

    public Optional<AssessmentUserProgressSnapshot> fetchUserProgressSnapshot(UUID userId) {
        if (internalServiceToken == null || internalServiceToken.isBlank()) {
            log.warn("assessment progress snapshot skipped: app.internal-service-token is not configured");
            return Optional.empty();
        }
        String url = assessmentBaseUrl + "/api/v1/assessment/internal/users/" + userId + "/progress-snapshot";
        HttpHeaders headers = new HttpHeaders();
        headers.set(TOKEN_HEADER, internalServiceToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return Optional.empty();
            }
            ApiResponse<AssessmentUserProgressSnapshot> wrapped = objectMapper.readValue(
                    response.getBody(), new TypeReference<ApiResponse<AssessmentUserProgressSnapshot>>() {});
            return Optional.ofNullable(wrapped.getData());
        } catch (RestClientException | IllegalArgumentException | JsonProcessingException ex) {
            log.warn("Failed to fetch assessment progress snapshot for user {}: {}", userId, ex.getMessage());
            return Optional.empty();
        }
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:8083";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
