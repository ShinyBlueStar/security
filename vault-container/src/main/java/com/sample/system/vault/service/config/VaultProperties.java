package com.sample.system.vault.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Validated
@ConfigurationProperties(prefix = "app.vault")
public record VaultProperties(
        @NotBlank String uri,
        String token,
        String roleId,
        String secretId,
        @NotBlank String transitMount,
        @NotBlank String pinKey,
        @NotBlank String pinEncryptKey,
        @NotBlank String cvv2Key,
        @NotBlank String cvv2EncryptKey,
        @NotBlank String otpHmacKey,
        @NotBlank String otpEncryptKey,
        @NotBlank String otpSharedSecretPath,
        @Positive int totpStepSeconds,
        @Positive int totpDigits,
        @Positive int totpAllowedDriftSteps
) {
}

