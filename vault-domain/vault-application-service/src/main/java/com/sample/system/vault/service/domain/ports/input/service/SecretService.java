package com.sample.system.vault.service.domain.ports.input.service;

import com.sample.system.vault.service.domain.exception.VaultDomainException;

public interface SecretService<GReq, GRes, VReq, VRes> {

    GRes generate(GReq request) throws VaultDomainException;

    VRes verify(VReq request) throws VaultDomainException;
}

