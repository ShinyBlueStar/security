package com.sample.system.vault.service.domain.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "Request body for CVV2 generation")
public class GenerateCvvRequest {
    @NotBlank(message = "cardId is required")
    @Schema(description = "Unique card identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    String cardId;

    @NotBlank(message = "encryptedData is required")
    @Schema(description = "Encrypted card data (Vault transit format)", example = "vault:v1:35OrZIcrH5Wi2Fbh4U32KunY8hGEYE9bDxOt6bHqlAI=", requiredMode = Schema.RequiredMode.REQUIRED)
    String encryptedData;
}
