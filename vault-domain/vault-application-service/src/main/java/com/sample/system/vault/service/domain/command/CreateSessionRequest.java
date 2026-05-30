package com.sample.system.vault.service.domain.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record CreateSessionRequest(
    @NotNull(message = "cardId is required")
    Long cardId,
    
    @NotBlank(message = "channel is required")
    String channel
) {}

