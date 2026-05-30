package com.sample.system.vault.service.domain.dtos;

import java.time.Instant;
import java.util.UUID;

public class Session {
    private final String sessionId;
    private final Long cardId;
    private final String channel;
    private final Instant createdAt;
    private Instant expiresAt;
    private boolean active;

    private Session(String sessionId, Long cardId, String channel, Instant createdAt, Instant expiresAt, boolean active) {
        this.sessionId = sessionId;
        this.cardId = cardId;
        this.channel = channel;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.active = active;
    }

    public static Session create(Long cardId, String channel, int validitySeconds) {
        Instant now = Instant.now();
        String sessionId = UUID.randomUUID().toString();
        Instant expiresAt = now.plusSeconds(validitySeconds);
        return new Session(sessionId, cardId, channel, now, expiresAt, true);
    }

    public boolean isValid() {
        return active && Instant.now().isBefore(expiresAt);
    }

    public void expire() {
        this.active = false;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Long getCardId() {
        return cardId;
    }

    public String getChannel() {
        return channel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isActive() {
        return active;
    }
}

