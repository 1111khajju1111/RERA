package com.rera.auditor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

/**
 * Thin client for the FastAPI AI microservice (Phase 4).
 * Kept separate from business logic so the AI service's actual
 * request/response shape can evolve without touching controllers.
 */
@Service
public class AiServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestClient restClient;

    public AiServiceClient(@Value("${ai-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public Map<?, ?> parseCad(Long projectId, String filePath) {
        // The backend and ai-service are deployed as two separate Render
        // services, each with its own disk — a local file_path is
        // meaningless on the other side. Read the bytes here and send
        // them along so ai-service never has to touch this filesystem.
        String fileContentBase64;
        try {
            byte[] bytes = Files.readAllBytes(Path.of(filePath));
            fileContentBase64 = Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Could not read uploaded file at " + filePath + " to send to the AI service: " + e.getMessage(), e);
        }

        return restClient.post()
            .uri("/parse-cad")
            .body(Map.of("project_id", projectId, "file_path", filePath, "file_content_base64", fileContentBase64))
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
