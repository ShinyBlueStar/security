package com.sample.system.vault.service.domain.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record ValidatePinRequest(
        @NotNull(message = "cardId is required")
        String cardId,

        @NotBlank(message = "hashedPin is required")
        String hashedPin,

        @NotBlank(message = "providedPin is required")
        String providedPin
) {}