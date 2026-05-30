package com.sample.system.vault.service.domain.ports.input.service;

import com.sample.system.vault.service.domain.dtos.Card;
import com.sample.system.vault.service.domain.exception.VaultDomainException;

public interface CvvService {
    boolean verifyCvv(String cardId, String encryptedCvv, String hashedCvv) throws VaultDomainException;
    Cvv2GenerationResult generateCvv2(String cardId, String encryptedCvv) throws VaultDomainException;
    record Cvv2GenerationResult(String encryptedCvv2, String hmacCvv2) {}
}

