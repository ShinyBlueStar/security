package com.sample.system.vault.service.domain.ports.output.repository;

import com.sample.system.vault.service.domain.dtos.Session;

import java.util.Optional;

public interface SessionRepository {
    Session save(Session session);
    Optional<Session> findById(String sessionId);
    void delete(String sessionId);
}

