package com.sample.system.vault.service.domain.response;

import java.time.Instant;

public record CreateSessionResponse(
    String sessionId,
    Long cardId,
    String channel,
    Instant createdAt,
    Instant expiresAt
) {}

