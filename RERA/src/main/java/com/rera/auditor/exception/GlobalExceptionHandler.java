package com.rera.auditor.exception;

import com.rera.auditor.dto.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;
import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), null);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthFailed(AuthenticationFailedException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation Failed", "One or more fields are invalid", details);
    }

    /**
     * Any non-2xx response from the AI service (parseCad/analyzeGis/etc.
     * via RestClient) lands here rather than the generic Exception
     * handler below. FastAPI's HTTPException responses carry a real
     * "detail" message (e.g. "Could not geocode address: ...") — surface
     * that instead of a one-size-fits-all 500, and log the raw body so a
     * non-JSON upstream failure (e.g. an IP being blocked, a gateway
     * timeout page) is still visible in this service's own logs.
     */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleAiServiceError(RestClientResponseException ex) {
        String upstreamBody = ex.getResponseBodyAsString();
        logger.warn("AI service returned {} : {}", ex.getStatusCode(), upstreamBody);

        String detail = extractDetail(upstreamBody);
        HttpStatus status = ex.getStatusCode().is4xxClientError()
            ? HttpStatus.valueOf(ex.getStatusCode().value())
            : HttpStatus.BAD_GATEWAY; // upstream 5xx is not this service's fault — report as a gateway problem
        return build(status, "AI Service Error", detail, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        logger.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
            "Something went wrong. Please try again.", null);
    }

    /** Pulls FastAPI's {"detail": "..."} out of the upstream error body, if present. */
    private String extractDetail(String upstreamBody) {
        if (upstreamBody == null || upstreamBody.isBlank()) {
            return "The AI service is currently unavailable. Please try again shortly.";
        }
        int idx = upstreamBody.indexOf("\"detail\"");
        if (idx == -1) {
            return "The AI service reported an error. Please try again shortly.";
        }
        int colon = upstreamBody.indexOf(':', idx);
        int firstQuote = upstreamBody.indexOf('"', colon + 1);
        int lastQuote = upstreamBody.indexOf('"', firstQuote + 1);
        if (firstQuote == -1 || lastQuote == -1) {
            return "The AI service reported an error. Please try again shortly.";
        }
        return upstreamBody.substring(firstQuote + 1, lastQuote);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String error, String message, List<String> details) {
        ApiErrorResponse body = new ApiErrorResponse(LocalDateTime.now(), status.value(), error, message, details);
        return ResponseEntity.status(status).body(body);
    }
}
