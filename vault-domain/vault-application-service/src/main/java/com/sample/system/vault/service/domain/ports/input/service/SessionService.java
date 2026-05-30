package com.sample.system.vault.service.domain.ports.input.service;

import com.sample.system.vault.service.domain.dtos.Session;
import com.sample.system.vault.service.domain.exception.VaultDomainException;

public interface SessionService {
    Session createSession(Long cardId, String channel) throws VaultDomainException;
    Session validateSession(String sessionId) throws VaultDomainException;
    void expireSession(String sessionId) throws VaultDomainException;
}

