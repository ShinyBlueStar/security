package com.sample.system.vault.service.domain.ports.input.service.impl;

import com.sample.system.vault.service.domain.dtos.Status;
import com.sample.system.vault.service.domain.exception.VaultDomainException;
import com.sample.system.vault.service.domain.ports.input.service.StatusService;
import com.sample.system.vault.service.domain.ports.output.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Log4j2
@Validated
@Service
@RequiredArgsConstructor
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;

    @Override
    public Status findByCode(String code) {
        return statusRepository.findByCode(code);
    }

    @Override
    @Transactional
    public Status createStatus(Status status) throws VaultDomainException {
        if (status == null || status.getCode() == null) {
            throw new VaultDomainException("Status code is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }

        Status existingStatus = statusRepository.findByCode(status.getCode());
        if (existingStatus != null && existingStatus.getId() != null) {
            throw new VaultDomainException(
                    "Status with code " + status.getCode() + " already exists",
                    StatusService.GENERAL_ERROR,
                    HttpStatus.BAD_REQUEST);
        }

        Status savedStatus = statusRepository.save(status);
        if (savedStatus == null || savedStatus.getId() == null) {
            throw new VaultDomainException(
                    "Could not save status with code " + status.getCode(),
                    StatusService.GENERAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return savedStatus;
    }

    @Override
    @Transactional
    public Status updateStatusPersianDescription(Status status) throws VaultDomainException {
        if (status == null || status.getCode() == null) {
            throw new VaultDomainException("Status code is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }
        if (status.getPersianDescription() == null) {
            throw new VaultDomainException("Persian description is required",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }

        Status existingStatus = statusRepository.findByCode(status.getCode());
        if (existingStatus == null || existingStatus.getId() == null) {
            throw new VaultDomainException("Status not found with code: " + status.getCode(),
                    StatusService.ID_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        existingStatus.setPersianDescription(status.getPersianDescription());
        Status savedStatus = statusRepository.save(existingStatus);
        if (savedStatus == null || savedStatus.getId() == null) {
            throw new VaultDomainException(
                    "Could not update status with code " + status.getCode(),
                    StatusService.GENERAL_ERROR,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return savedStatus;
    }

    @Override
    public Page<Status> listStatuses(Map<String, String> params, String caller, String ip) throws VaultDomainException {
        log.info("Starting status list retrieval service");
        try {
            Page<Status> allStatuses = statusRepository.findAllStatuses(params);
            return allStatuses;
        } catch (Exception e) {
            log.error("Error occurred in status list retrieval service - error: {}", e.getMessage(), e);
            throw new VaultDomainException("", StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
