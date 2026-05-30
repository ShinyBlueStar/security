package com.sample.system.vault.service.domain.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "Request body for PIN1 generation")
public class GeneratePin1Request {
    @NotBlank(message = "cardId is required")
    @Schema(description = "Unique card identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    String cardId;
}