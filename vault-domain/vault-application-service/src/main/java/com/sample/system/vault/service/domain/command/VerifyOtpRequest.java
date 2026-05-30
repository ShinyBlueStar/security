package com.sample.system.vault.service.domain.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for OTP (PIN2) verification")
public record VerifyOtpRequest(
        @NotBlank(message = "cardId is required")
        @Schema(description = "Unique card identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        String cardId,
        @NotBlank(message = "providedOtp is required")
        @Schema(description = "Encrypted OTP provided by user for verification", example = "vault:v1:zRBZSPHAoUdqydoHoGteS0VmmQBpOVu0bdbUSo/6cjmlzA==", requiredMode = Schema.RequiredMode.REQUIRED)
        String providedOtp
) {}