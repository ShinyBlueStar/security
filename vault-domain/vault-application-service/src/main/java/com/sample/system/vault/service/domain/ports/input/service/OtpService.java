package com.sample.system.vault.service.domain.ports.input.service;

import com.sample.system.vault.service.domain.exception.VaultDomainException;

import java.time.Instant;

public interface OtpService {
    OtpGenerationResult generateOtp(String cardId) throws VaultDomainException;
    Boolean verifyOtp(String cardId, String providedOtp);

    record OtpGenerationResult(String encryptedOtp, String hashOtp, Instant expireAt) {}
}