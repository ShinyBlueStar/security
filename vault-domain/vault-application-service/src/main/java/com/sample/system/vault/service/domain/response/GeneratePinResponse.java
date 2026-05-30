package com.sample.system.vault.service.domain.response;

public record GeneratePinResponse (
        String encryptedPin,
        String hashPin
){}
