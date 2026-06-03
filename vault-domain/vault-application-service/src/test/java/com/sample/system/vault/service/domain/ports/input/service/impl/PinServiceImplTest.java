package com.sample.system.vault.service.domain.ports.input.service.impl;

import com.sample.system.vault.service.domain.command.GeneratePin1Request;
import com.sample.system.vault.service.domain.command.ValidatePinRequest;
import com.sample.system.vault.service.domain.exception.VaultDomainException;
import com.sample.system.vault.service.domain.ports.input.service.StatusService;
import com.sample.system.vault.service.domain.ports.output.repository.CryptoRepository;
import com.sample.system.vault.service.domain.response.GeneratePinResponse;
import com.sample.system.vault.service.domain.response.ValidatePinResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.vault.VaultException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class PinServiceImplTest {

    private CryptoRepository cryptoRepository;
    private PinServiceImpl service;

    @BeforeEach
    void setUp() {
        cryptoRepository = Mockito.mock(CryptoRepository.class);
        service = new PinServiceImpl(cryptoRepository);

        ReflectionTestUtils.setField(service, "pinKey", "pin-hmac-key");
        ReflectionTestUtils.setField(service, "pinEncryptKey", "pin-encrypt-key");
    }

    private static GeneratePin1Request generateRequest(String cardId) {
        GeneratePin1Request request = new GeneratePin1Request();
        ReflectionTestUtils.setField(request, "cardId", cardId);
        return request;
    }

    @Test
    void generate_returnsEncryptedPinAndHmac() throws Exception {
        when(cryptoRepository.encrypt(eq("pin-encrypt-key"), any())).thenReturn("encryptedPin");
        when(cryptoRepository.hmac(eq("pin-hmac-key"), any())).thenReturn("pinHmac");

        GeneratePinResponse response = service.generate(generateRequest("card-1"));

        assertEquals("encryptedPin", response.encryptedPin());
        assertEquals("pinHmac", response.hashPin());
    }

    @Test
    void generate_whenCryptoRepositoryFails_throwsVaultDomainException() throws Exception {
        when(cryptoRepository.encrypt(anyString(), any())).thenThrow(new VaultException("vault down"));

        VaultDomainException ex = assertThrows(VaultDomainException.class,
                () -> service.generate(generateRequest("card-1")));

        assertEquals(StatusService.PIN_GENERATION_ERROR, ex.getStatus());
    }

    @Test
    void verify_whenHmacMatches_returnsValidTrue() throws Exception {
        byte[] decrypted = "1234".getBytes(StandardCharsets.UTF_8);
        when(cryptoRepository.decrypt("pin-encrypt-key", "encProvidedPin")).thenReturn(decrypted);
        when(cryptoRepository.hmac("pin-hmac-key", decrypted)).thenReturn("matchingHmac");

        ValidatePinResponse response = service.verify(
                new ValidatePinRequest("card-1", "matchingHmac", "encProvidedPin"));

        assertTrue(response.valid());
    }

    @Test
    void verify_whenHmacDoesNotMatch_returnsValidFalse() throws Exception {
        byte[] decrypted = "1234".getBytes(StandardCharsets.UTF_8);
        when(cryptoRepository.decrypt("pin-encrypt-key", "encProvidedPin")).thenReturn(decrypted);
        when(cryptoRepository.hmac("pin-hmac-key", decrypted)).thenReturn("computedHmac");

        ValidatePinResponse response = service.verify(
                new ValidatePinRequest("card-1", "differentHmac", "encProvidedPin"));

        assertFalse(response.valid());
    }

    @Test
    void verify_whenDecryptThrows_throwsVaultDomainException() throws Exception {
        when(cryptoRepository.decrypt(anyString(), anyString())).thenThrow(new VaultException("bad ciphertext"));

        VaultDomainException ex = assertThrows(VaultDomainException.class,
                () -> service.verify(new ValidatePinRequest("card-1", "hash", "enc")));

        assertEquals(StatusService.PIN_IS_INVALID, ex.getStatus());
    }
}
