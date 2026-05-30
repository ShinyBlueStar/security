package com.sample.system.vault.service.domain.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for CVV2 verification")
public record VerifyCvvRequest(
        @NotBlank(message = "cardId is required")
        @Schema(description = "Unique card identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        String cardId,
        @NotBlank(message = "hashedCvv is required")
        @Schema(description = "Hashed CVV2 stored in database (Vault transit format)", example = "vault:v1:+XuoreGfT5Dt8HVfZQSZYf8CyJazqpuv+bPoBi7Xr2I=", requiredMode = Schema.RequiredMode.REQUIRED)
        String hashedCvv,
        @NotBlank(message = "encryptedData is required")
        @Schema(description = "Encrypted card data (Vault transit format)", example = "vault:v1:35OrZIcrH5Wi2Fbh4U32KunY8hGEYE9bDxOt6bHqlAI=", requiredMode = Schema.RequiredMode.REQUIRED)
        String encryptedData
//        @NotBlank(message = "pan is required")
//        @Pattern(regexp = "\\d{16}", message = "PAN must be exactly 16 digits")
//        String pan,
//        @NotBlank(message = "expTime is required")
//        @Pattern(regexp = "\\d{4}", message = "EXP TIME must be exactly 4 digits")
//        String expTime,
//        @NotBlank(message = "serviceCode is required")
//        String serviceCode
) {
}