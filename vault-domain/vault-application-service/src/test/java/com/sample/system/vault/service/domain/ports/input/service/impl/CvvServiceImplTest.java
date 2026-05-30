package com.sample.system.vault.service.domain.ports.input.service.impl;

import com.sample.system.vault.service.domain.command.VerifyCvvRequest;
import com.sample.system.vault.service.domain.exception.VaultDomainException;
import com.sample.system.vault.service.domain.ports.input.service.StatusService;
import com.sample.system.vault.service.domain.ports.output.repository.CryptoRepository;
import com.sample.system.vault.service.domain.response.VerifyCvvResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.vault.VaultException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class CvvServiceImplTest {

    private CryptoRepository cryptoRepository;
    private CvvServiceImpl service;

    @BeforeEach
    void setUp() {
        cryptoRepository = Mockito.mock(CryptoRepository.class);
        service = new CvvServiceImpl(cryptoRepository);

        // @Value fields
        ReflectionTestUtils.setField(service, "cvv2Key", "cvv2-key");
        ReflectionTestUtils.setField(service, "cvv2EncryptKey", "cvv2-encrypt-key");
        ReflectionTestUtils.setField(service, "cvv2Length", 4);
    }

    /**
     * Builds a vault:v1:<base64> string that derives to CVV2 "1234"
     * using deriveCvvFromCmac() implementation.
     */
    private static String hashedCvvDerivingTo1234() {
        byte[] bytes = new byte[] {0x00, 0x00, 0x00, (byte) 0xEA, 0x10}; // hex: 000000ea10 -> offset=0 -> binary=234 -> 1000+234=1234
        return "vault:v1:" + Base64.getEncoder().encodeToString(bytes);
    }

    @Test
    void verify_whenPlainDataMatchesDerivedCvv_returnsValidTrue() throws Exception {
        String cardId = "550e8400-e29b-41d4-a716-446655440000";
        String encryptedData = "vault:v1:dummy";
        String hashedCvv = hashedCvvDerivingTo1234();

        when(cryptoRepository.decrypt(anyString(), eq(encryptedData)))
                .thenReturn("1234".getBytes(StandardCharsets.UTF_8));
        when(cryptoRepository.hmac(anyString(), any()))
                .thenReturn("ignored");

        VerifyCvvResponse response = service.verify(new VerifyCvvRequest(cardId, hashedCvv, encryptedData));

        assertTrue(response.valid());
    }

    @Test
    void verify_whenPlainDataDoesNotMatchDerivedCvv_returnsValidFalse() throws Exception {
        String cardId = "550e8400-e29b-41d4-a716-446655440000";
        String encryptedData = "vault:v1:dummy";
        String hashedCvv = hashedCvvDerivingTo1234();

        when(cryptoRepository.decrypt(anyString(), eq(encryptedData)))
                .thenReturn("9999".getBytes(StandardCharsets.UTF_8));
        when(cryptoRepository.hmac(anyString(), any()))
                .thenReturn("ignored");

        VerifyCvvResponse response = service.verify(new VerifyCvvRequest(cardId, hashedCvv, encryptedData));

        assertFalse(response.valid());
    }

    @Test
    void verify_whenPlainDataIsNotNumeric_throwsVaultDomainException() throws Exception {
        String cardId = "550e8400-e29b-41d4-a716-446655440000";
        String encryptedData = "vault:v1:dummy";
        String hashedCvv = hashedCvvDerivingTo1234();

        when(cryptoRepository.decrypt(anyString(), eq(encryptedData)))
                .thenReturn("12a4".getBytes(StandardCharsets.UTF_8));
        when(cryptoRepository.hmac(anyString(), any()))
                .thenReturn("ignored");

        VaultDomainException ex = assertThrows(VaultDomainException.class,
                () -> service.verify(new VerifyCvvRequest(cardId, hashedCvv, encryptedData)));

        assertEquals(StatusService.INPUT_PARAMETER_NOT_VALID, ex.getStatus());
        assertTrue(ex.getMessage().toLowerCase().contains("format"));
    }

    @Test
    void verify_whenDecryptThrowsVaultException_throwsVaultDomainException() throws Exception {
        String cardId = "550e8400-e29b-41d4-a716-446655440000";
        String encryptedData = "vault:v1:dummy";
        String hashedCvv = hashedCvvDerivingTo1234();

        when(cryptoRepository.decrypt(anyString(), eq(encryptedData)))
                .thenThrow(new VaultException("invalid ciphertext"));

        VaultDomainException ex = assertThrows(VaultDomainException.class,
                () -> service.verify(new VerifyCvvRequest(cardId, hashedCvv, encryptedData)));

        assertEquals(StatusService.INPUT_PARAMETER_NOT_VALID, ex.getStatus());
        assertTrue(ex.getMessage().toLowerCase().contains("encrypteddata"));
    }
}

