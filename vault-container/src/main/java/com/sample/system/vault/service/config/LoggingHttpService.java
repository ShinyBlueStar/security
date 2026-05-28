package com.sample.system.vault.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for logging HTTP requests and responses.
 * Used by RequestBodyInterceptor and ResponseBodyInterceptor.
 */
@Slf4j
@Service
public class LoggingHttpService {

    private final ObjectMapper objectMapper;

    public LoggingHttpService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    /**
     * Logs HTTP request details including headers and body.
     *
     * @param request HTTP servlet request
     * @param body    Request body object
     */
    public void displayReq(HttpServletRequest request, Object body) {
        try {
            Map<String, Object> requestInfo = new HashMap<>();
            requestInfo.put("method", request.getMethod());
            requestInfo.put("uri", request.getRequestURI());
            requestInfo.put("queryString", request.getQueryString());
            requestInfo.put("remoteAddr", request.getRemoteAddr());

            // Add headers
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }
            requestInfo.put("headers", headers);

            // Add body (mask sensitive data if needed)
            if (body != null) {
                String bodyJson = objectMapper.writeValueAsString(body);
                requestInfo.put("body", maskSensitiveData(bodyJson));
            }

            log.info("HTTP Request: {}", objectMapper.writeValueAsString(requestInfo));
        } catch (Exception e) {
            log.warn("Failed to log HTTP request: {}", e.getMessage());
        }
    }

    /**
     * Logs HTTP response details including status and body.
     *
     * @param request  HTTP servlet request
     * @param response HTTP servlet response
     * @param body     Response body object
     */
    public void displayResp(HttpServletRequest request, HttpServletResponse response, Object body) {
        try {
            Map<String, Object> responseInfo = new HashMap<>();
            responseInfo.put("status", response.getStatus());
            responseInfo.put("uri", request.getRequestURI());

            // Add response headers
            Map<String, String> headers = new HashMap<>();
            response.getHeaderNames().forEach(headerName ->
                    headers.put(headerName, response.getHeader(headerName))
            );
            responseInfo.put("headers", headers);

            // Add body (mask sensitive data if needed)
            if (body != null) {
                String bodyJson = objectMapper.writeValueAsString(body);
                responseInfo.put("body", maskSensitiveData(bodyJson));
            }

            log.info("HTTP Response: {}", objectMapper.writeValueAsString(responseInfo));
        } catch (Exception e) {
            log.warn("Failed to log HTTP response: {}", e.getMessage());
        }
    }

    /**
     * Masks sensitive data in JSON strings (e.g., passwords, PINs, CVV2).
     *
     * @param json JSON string to mask
     * @return Masked JSON string
     */
    private String maskSensitiveData(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }

        // List of sensitive field names (case-insensitive)
        String[] sensitiveFields = {
                "password", "pin", "pincode", "cvv2", "cvv", "secret",
                "token", "apiKey", "accessToken", "refreshToken", "otp"
        };

        String masked = json;
        for (String field : sensitiveFields) {
            // Pattern to match field names in JSON (e.g., "password":"value")
            String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]+)\"";
            masked = masked.replaceAll("(?i)" + pattern, "\"" + field + "\":\"***\"");
        }

        return masked;
    }
}

