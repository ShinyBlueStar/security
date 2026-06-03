package com.sample.system.vault.service.domain.ports.input.service.impl;

import com.sample.system.vault.service.domain.dtos.Session;
import com.sample.system.vault.service.domain.exception.VaultDomainException;
import com.sample.system.vault.service.domain.ports.output.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionServiceImplTest {

    private SessionRepository sessionRepository;
    private SessionServiceImpl service;

    @BeforeEach
    void setUp() {
        sessionRepository = Mockito.mock(SessionRepository.class);
        service = new SessionServiceImpl(sessionRepository);
    }

    @Test
    void createSession_withInvalidCardId_throwsVaultDomainException() {
        assertThrows(VaultDomainException.class, () -> service.createSession(0L, "web"));
        assertThrows(VaultDomainException.class, () -> service.createSession(null, "web"));
    }

    @Test
    void createSession_withBlankChannel_throwsVaultDomainException() {
        assertThrows(VaultDomainException.class, () -> service.createSession(1L, " "));
    }

    @Test
    void createSession_withValidInput_savesAndReturnsSession() throws Exception {
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Session session = service.createSession(1L, "web");

        assertTrue(session.isValid());
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void validateSession_withUnknownId_throwsVaultDomainException() {
        when(sessionRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(VaultDomainException.class, () -> service.validateSession("missing"));
    }

    @Test
    void validateSession_withExpiredSession_throwsVaultDomainException() {
        Session expired = Session.create(1L, "web", -1);
        when(sessionRepository.findById(expired.getSessionId())).thenReturn(Optional.of(expired));

        assertThrows(VaultDomainException.class, () -> service.validateSession(expired.getSessionId()));
    }

    @Test
    void validateSession_withActiveSession_returnsIt() throws Exception {
        Session active = Session.create(1L, "web", 300);
        when(sessionRepository.findById(active.getSessionId())).thenReturn(Optional.of(active));

        Session result = service.validateSession(active.getSessionId());

        assertTrue(result.isValid());
    }

    @Test
    void expireSession_withExistingSession_expiresAndSavesIt() throws Exception {
        Session active = Session.create(1L, "web", 300);
        when(sessionRepository.findById(active.getSessionId())).thenReturn(Optional.of(active));

        service.expireSession(active.getSessionId());

        assertFalse(active.isActive());
        verify(sessionRepository, times(1)).save(active);
    }

    @Test
    void expireSession_withUnknownId_doesNothing() throws Exception {
        when(sessionRepository.findById("missing")).thenReturn(Optional.empty());

        service.expireSession("missing");

        verify(sessionRepository, never()).save(any(Session.class));
    }
}
