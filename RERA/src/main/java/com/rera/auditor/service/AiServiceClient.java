package com.rera.auditor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Thin client for the FastAPI AI microservice (Phase 4).
 * Kept separate from business logic so the AI service's actual
 * request/response shape can evolve without touching controllers.
 */
@Service
public class AiServiceClient {

    private final RestClient restClient;

    public AiServiceClient(@Value("${ai-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public Map<?, ?> parseCad(Long projectId, String filePath) {
        return restClient.post()
            .uri("/parse-cad")
            .body(Map.of("project_id", projectId, "file_path", filePath))
            .retrieve()
            .body(Map.class);
    }

    public Map<?, ?> runCompliance(Long projectId) {
        return restClient.post()
            .uri("/run-compliance")
            .body(Map.of("project_id", projectId))
            .retrieve()
            .body(Map.class);
    }

    public Map<?, ?> generateSuggestions(Long projectId) {
        return restClient.post()
            .uri("/generate-suggestions")
            .body(Map.of("project_id", projectId))
            .retrieve()
            .body(Map.class);
    }

    public String chat(Long projectId, String message) {
        Map<?, ?> response = restClient.post()
            .uri("/chat")
            .body(Map.of("project_id", projectId, "message", message))
            .retrieve()
            .body(Map.class);
        return response != null ? String.valueOf(response.get("reply")) : "AI service unavailable.";
    }

    /**
     * Synchronous, unlike the CAD pipeline — geocoding + an Overpass query
     * typically completes in a few seconds, and the GIS page is waiting on
     * a direct result to render the map, so fire-and-forget @Async doesn't
     * fit here the way it did for CAD parsing.
     */
    public Map<?, ?> analyzeGis(Long projectId, String address) {
        return restClient.post()
            .uri("/gis/analyze")
            .body(Map.of("project_id", projectId, "address", address))
            .retrieve()
            .body(Map.class);
    }

    public Map<?, ?> generateReport(Long projectId, String format) {
        return restClient.post()
            .uri("/reports/generate")
            .body(Map.of("project_id", projectId, "format", format))
            .retrieve()
            .body(Map.class);
    }
}
