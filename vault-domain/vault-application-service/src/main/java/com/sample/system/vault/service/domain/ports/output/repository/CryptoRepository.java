package com.sample.system.vault.service.domain.ports.output.repository;

import com.sample.system.vault.service.domain.exception.VaultDomainException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface CryptoRepository {

    String encrypt(String keyName, byte[] plaintext) throws VaultDomainException;
    byte[] decrypt(String keyName, String ciphertext) throws VaultDomainException;
    String hmac(String keyName, byte[] input) throws VaultDomainException;
    boolean verifyHmac(String keyName, byte[] data, String expectedHmac) throws VaultDomainException;
    String calculateCvv2_AES_CMAC(String data, String keyName) throws VaultDomainException;
}