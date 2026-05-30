package com.sample.system.vault.service.domain.ports.input.service;

import com.sample.system.vault.service.domain.dtos.Status;
import com.sample.system.vault.service.domain.exception.VaultDomainException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * Status Application Service Interface
 * Defines operations for Status management
 * Following Hexagonal Architecture - Input Port
 */
public interface StatusService {

    int GENERAL_ERROR = 999;
    int PIN_IS_INVALID = 1;
    int CVV2_GENERATION_ERROR = 2;
    int CVV2_VALIDATION_ERROR = 3;
    int USER_NOT_PERMISSION = 4;
    int TOKEN_NOT_VALID = 5;
    int INPUT_PARAMETER_NOT_VALID = 6;
    int CONVERT_TO_CVV2_ERROR = 7;
    int PIN_GENERATION_ERROR = 8;
    int ID_NOT_FOUND = 11;


    Status findByCode(String code);

    Status createStatus(@Valid Status status) throws VaultDomainException;

    Status updateStatusPersianDescription(@Valid Status status) throws VaultDomainException;

    Page<Status> listStatuses(Map<String, String> map, String caller, String ip) throws VaultDomainException;

}
