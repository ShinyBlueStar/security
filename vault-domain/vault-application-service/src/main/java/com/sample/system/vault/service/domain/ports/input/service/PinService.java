package com.sample.system.vault.service.domain.ports.input.service;

import com.sample.system.vault.service.domain.dtos.Card;
import com.sample.system.vault.service.domain.exception.VaultDomainException;

public interface PinService {
    Boolean validatePin1(String cardId, String hashedPin, String providedPin)
            throws VaultDomainException;
    Card generatePin1(String cardId) throws VaultDomainException;
    void setUserDefinedPin(String keyName, String pin, String cardId) throws VaultDomainException;
}

