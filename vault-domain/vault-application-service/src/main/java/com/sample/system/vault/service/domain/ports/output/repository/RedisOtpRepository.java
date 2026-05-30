package com.sample.system.vault.service.domain.ports.output.repository;

import java.time.Instant;

public interface RedisOtpRepository {

    void saveOtp(String sessionId, String hmac, Instant expireAt);
    String getOtp(String cardId);
    Instant getExpireAt(String cardId);
    int incrementRetry(String cardId);
    int incrementRetry(String cardId, Instant expireAt);
    int getRetryCount(String cardId);
    void deleteOtp(String cardId);
}
