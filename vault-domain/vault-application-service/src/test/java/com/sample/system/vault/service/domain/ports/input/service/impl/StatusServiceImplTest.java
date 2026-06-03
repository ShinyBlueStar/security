package com.sample.system.vault.service.domain.ports.input.service.impl;

import com.sample.system.vault.service.domain.dtos.Status;
import com.sample.system.vault.service.domain.exception.VaultDomainException;
import com.sample.system.vault.service.domain.ports.input.service.StatusService;
import com.sample.system.vault.service.domain.ports.output.repository.StatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

class StatusServiceImplTest {

    private StatusRepository statusRepository;
    private StatusServiceImpl service;

    @BeforeEach
    void setUp() {
        statusRepository = Mockito.mock(StatusRepository.class);
        service = new StatusServiceImpl(statusRepository);
    }

    private static Status statusWith(Long id, String code) {
        return new Status(id, code, "توضیحات");
    }

    @Test
    void createStatus_withoutCode_throwsVaultDomainException() {
        VaultDomainException ex = assertThrows(VaultDomainException.class,
                () -> service.createStatus(new Status(null, null, "x")));

        assertEquals(StatusService.INPUT_PARAMETER_NOT_VALID, ex.getStatus());
    }

    @Test
    void createStatus_whenCodeAlreadyExists_throwsVaultDomainException() {
        when(statusRepository.findByCode("PIN_INVALID")).thenReturn(statusWith(1L, "PIN_INVALID"));

        VaultDomainException ex = assertThrows(VaultDomainException.class,
                () -> service.createStatus(statusWith(null, "PIN_INVALID")));

        assertEquals(StatusService.GENERAL_ERROR, ex.getStatus());
    }

    @Test
    void createStatus_withNewCode_savesAndReturnsIt() throws Exception {
        when(statusRepository.findByCode("NEW_CODE")).thenReturn(null);
        Status saved = statusWith(2L, "NEW_CODE");
        when(statusRepository.save(saved)).thenReturn(saved);

        Status result = service.createStatus(saved);

        assertEquals(saved, result);
    }

    @Test
    void updateStatusPersianDescription_whenNotFound_throwsVaultDomainException() {
        when(statusRepository.findByCode("MISSING")).thenReturn(null);

        VaultDomainException ex = assertThrows(VaultDomainException.class,
                () -> service.updateStatusPersianDescription(statusWith(null, "MISSING")));

        assertEquals(StatusService.ID_NOT_FOUND, ex.getStatus());
    }

    @Test
    void updateStatusPersianDescription_whenFound_updatesAndSaves() throws Exception {
        Status existing = statusWith(3L, "EXISTING");
        Status update = new Status(null, "EXISTING", "توضیحات جدید");
        when(statusRepository.findByCode("EXISTING")).thenReturn(existing);
        when(statusRepository.save(existing)).thenReturn(existing);

        Status result = service.updateStatusPersianDescription(update);

        assertEquals("توضیحات جدید", result.getPersianDescription());
    }

    @Test
    void listStatuses_whenRepositoryFails_throwsVaultDomainException() {
        when(statusRepository.findAllStatuses(anyMap())).thenThrow(new RuntimeException("db down"));

        VaultDomainException ex = assertThrows(VaultDomainException.class,
                () -> service.listStatuses(Map.of(), "caller", "127.0.0.1"));

        assertEquals(StatusService.GENERAL_ERROR, ex.getStatus());
    }

    @Test
    void listStatuses_returnsRepositoryPage() throws Exception {
        Page<Status> page = new PageImpl<>(java.util.List.of(statusWith(1L, "A")));
        when(statusRepository.findAllStatuses(anyMap())).thenReturn(page);

        Page<Status> result = service.listStatuses(Map.of(), "caller", "127.0.0.1");

        assertEquals(1, result.getTotalElements());
    }
}
