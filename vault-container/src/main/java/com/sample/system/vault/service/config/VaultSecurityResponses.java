package com.sample.system.vault.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.system.vault.service.domain.ports.input.service.StatusService;
import com.sample.system.vault.service.domain.response.base.BaseResponse;
import com.sample.system.vault.service.domain.response.base.ErrorDetail;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Renders auth failures in the same BaseResponse/ErrorDetail shape the rest of the API uses.
 */
final class VaultSecurityResponses {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private VaultSecurityResponses() {
    }

    static AuthenticationEntryPoint unauthorized() {
        return (request, response, authException) -> write(response, HttpServletResponse.SC_UNAUTHORIZED,
                StatusService.TOKEN_NOT_VALID, "Missing or invalid bearer token");
    }

    static AccessDeniedHandler forbidden() {
        return (request, response, accessDeniedException) -> write(response, HttpServletResponse.SC_FORBIDDEN,
                StatusService.USER_NOT_PERMISSION, "Access is denied");
    }

    private static void write(HttpServletResponse response, int httpStatus, int statusCode, String message) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorDetail errorDetail = new ErrorDetail(message, String.valueOf(statusCode));
        response.getWriter().write(MAPPER.writeValueAsString(new BaseResponse<>(false, errorDetail)));
    }
}
