package com.sample.system.vault.service.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Checked exception carrying a domain status code (see StatusService constants)
 * and the HTTP status the REST layer should respond with.
 */
@Getter
public class VaultDomainException extends Exception {

    private static final int DEFAULT_STATUS = 999;

    private final int status;
    private final HttpStatus httpStatus;

    public VaultDomainException(String message) {
        this(message, DEFAULT_STATUS, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public VaultDomainException(String message, int status, HttpStatus httpStatus) {
        super(message);
        this.status = status;
        this.httpStatus = httpStatus;
    }
}
