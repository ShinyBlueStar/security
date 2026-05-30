package com.sample.system.vault.service.domain.response;

import java.time.Instant;

public record GenerateOtpResponse(
        String encryptedOtp,
        String hashOtp,
        Instant expireAt
) {}