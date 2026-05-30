package com.sample.system.vault.service.domain.ports.input.service.impl;

import com.sample.system.vault.service.domain.dtos.Session;
import com.sample.system.vault.service.domain.ports.input.service.SessionService;
import com.sample.system.vault.service.domain.ports.output.repository.SessionRepository;
import com.sample.system.vault.service.domain.exception.VaultDomainException;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceImpl implements SessionService {

    private static final int SESSION_VALIDITY_SECONDS = 300; // 5 minutes

    private final SessionRepository sessionRepository;

    public SessionServiceImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Session createSession(Long cardId, String channel) throws VaultDomainException {
        if (cardId == null || cardId <= 0) {
            throw new VaultDomainException("Invalid cardId");
        }
        if (channel == null || channel.isBlank()) {
            throw new VaultDomainException("Channel must not be empty");
        }

        Session session = Session.create(cardId, channel, SESSION_VALIDITY_SECONDS);
        return sessionRepository.save(session);
    }

    @Override
    public Session validateSession(String sessionId) throws VaultDomainException {
        if (sessionId == null || sessionId.isBlank()) {
            throw new VaultDomainException("SessionId must not be empty");
        }

        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new VaultDomainException("Session not found: " + sessionId));

        if (!session.isValid()) {
            throw new VaultDomainException("Session expired or inactive: " + sessionId);
        }

        return session;
    }

    @Override
    public void expireSession(String sessionId) throws VaultDomainException {
        if (sessionId == null || sessionId.isBlank()) {
            throw new VaultDomainException("SessionId must not be empty");
        }

        sessionRepository.findById(sessionId)
            .ifPresent(session -> {
                session.expire();
                sessionRepository.save(session);
            });
    }
}

