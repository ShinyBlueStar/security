package com.sample.system.vault.service.domain.ports.input.service.impl;

import com.sample.system.vault.service.domain.command.GenerateOtpRequest;
import com.sample.system.vault.service.domain.command.VerifyOtpRequest;
import com.sample.system.vault.service.domain.exception.VaultDomainException;
import com.sample.system.vault.service.domain.ports.input.service.StatusService;
import com.sample.system.vault.service.domain.ports.output.repository.CryptoRepository;
import com.sample.system.vault.service.domain.ports.output.repository.RedisOtpRepository;
import com.sample.system.vault.service.domain.response.GenerateOtpResponse;
import com.sample.system.vault.service.domain.response.VerifyOtpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.vault.VaultException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OtpServiceImplTest {

    private CryptoRepository cryptoRepository;
    private RedisOtpRepository redisOtpRepository;
    private OtpServiceImpl service;

    @BeforeEach
    void setUp() {
        cryptoRepository = Mockito.mock(CryptoRepository.class);
        redisOtpRepository = Mockito.mock(RedisOtpRepository.class);
        service = new OtpServiceImpl(new RestTemplate(), redisOtpRepository, cryptoRepository);

        ReflectionTestUtils.setField(service, "otpHmacKey", "otp-hmac-key");
        ReflectionTestUtils.setField(service, "otpEncryptKey", "otp-encrypt-key");
        ReflectionTestUtils.setField(service, "otpValiditySeconds", 115);
    }

    private static GenerateOtpRequest generateRequest(String cardId) {
        GenerateOtpRequest request = new GenerateOtpRequest();
        ReflectionTestUtils.setField(request, "cardId", cardId);
        return request;
    }

    @Test
    void generate_savesHmacInRedisAndReturnsEncryptedOtp() throws Exception {
        when(cryptoRepository.encrypt(eq("otp-encrypt-key"), any())).thenReturn("encryptedOtp");
        when(cryptoRepository.hmac(eq("otp-hmac-key"), any())).thenReturn("otpHmac");

        GenerateOtpResponse response = service.generate(generateRequest("card-1"));

        assertEquals("encryptedOtp", response.encryptedOtp());
        assertEquals("otpHmac", response.hashOtp());
        verify(redisOtpRepository).saveOtp(eq("card-1"), eq("otpHmac"), any(Instant.class));
    }

    @Test
    void verify_whenHmacMatchesAndNotExpired_returnsValidTrueAndDeletesOtp() throws Exception {
        byte[] decrypted = "654321".getBytes(StandardCharsets.UTF_8);
        when(cryptoRepository.decrypt("otp-encrypt-key", "encProvidedOtp")).thenReturn(decrypted);
        when(redisOtpRepository.getExpireAt("card-1")).thenReturn(Instant.now().plusSeconds(60));
        when(redisOtpRepository.getOtp("card-1")).thenReturn("matchingHmac");
        when(cryptoRepository.hmac("otp-hmac-key", decrypted)).thenReturn("matchingHmac");

        VerifyOtpResponse response = service.verify(new VerifyOtpRequest("card-1", "encProvidedOtp"));

        assertTrue(response.valid());
        verify(redisOtpRepository).deleteOtp("card-1");
        verify(redisOtpRepository, never()).incrementRetry(anyString(), any(Instant.class));
    }

    @Test
    void verify_whenHmacDoesNotMatch_returnsValidFalseAndIncrementsRetry() throws Exception {
        byte[] decrypted = "654321".getBytes(StandardCharsets.UTF_8);
        Instant expireAt = Instant.now().plusSeconds(60);
        when(cryptoRepository.decrypt("otp-encrypt-key", "encProvidedOtp")).thenReturn(decrypted);
        when(redisOtpRepository.getExpireAt("card-1")).thenReturn(expireAt);
        when(redisOtpRepository.getOtp("card-1")).thenReturn("expectedHmac");
        when(cryptoRepository.hmac("otp-hmac-key", decrypted)).thenReturn("computedHmac");

        VerifyOtpResponse response = service.verify(new VerifyOtpRequest("card-1", "encProvidedOtp"));

        assertFalse(response.valid());
        verify(redisOtpRepository, times(1)).incrementRetry("card-1", expireAt);
    }

    @Test
    void verify_whenExpired_throwsVaultDomainException() throws Exception {
        byte[] decrypted = "654321".getBytes(StandardCharsets.UTF_8);
        when(cryptoRepository.decrypt("otp-encrypt-key", "encProvidedOtp")).thenReturn(decrypted);
        when(redisOtpRepository.getExpireAt("card-1")).thenReturn(Instant.now().minusSeconds(1));

        VaultDomainException ex = assertThrows(VaultDomainException.class,
                () -> service.verify(new VerifyOtpRequest("card-1", "encProvidedOtp")));

        assertEquals(StatusService.INPUT_PARAMETER_NOT_VALID, ex.getStatus());
    }

    @Test
    void verify_whenNotGenerated_throwsVaultDomainException() throws Exception {
        byte[] decrypted = "654321".getBytes(StandardCharsets.UTF_8);
        when(cryptoRepository.decrypt("otp-encrypt-key", "encProvidedOtp")).thenReturn(decrypted);
        when(redisOtpRepository.getExpireAt("card-1")).thenReturn(null);

        VaultDomainException ex = assertThrows(VaultDomainException.class,
                () -> service.verify(new VerifyOtpRequest("card-1", "encProvidedOtp")));

        assertEquals(StatusService.INPUT_PARAMETER_NOT_VALID, ex.getStatus());
    }

    @Test
    void verify_whenDecryptThrows_throwsVaultDomainException() throws Exception {
        when(cryptoRepository.decrypt(anyString(), anyString())).thenThrow(new VaultException("bad ciphertext"));

        VaultDomainException ex = assertThrows(VaultDomainException.class,
                () -> service.verify(new VerifyOtpRequest("card-1", "encProvidedOtp")));

        assertEquals(StatusService.INPUT_PARAMETER_NOT_VALID, ex.getStatus());
    }
}
