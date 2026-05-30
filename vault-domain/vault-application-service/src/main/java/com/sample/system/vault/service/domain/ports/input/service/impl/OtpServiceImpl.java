package com.sample.system.vault.service.domain.ports.input.service.impl;

import com.sample.system.vault.service.domain.command.GenerateOtpRequest;
import com.sample.system.vault.service.domain.command.VerifyOtpRequest;
import com.sample.system.vault.service.domain.exception.VaultDomainException;
import com.sample.system.vault.service.domain.ports.input.service.SecretService;
import com.sample.system.vault.service.domain.ports.input.service.StatusService;
import com.sample.system.vault.service.domain.ports.output.repository.CryptoRepository;
import com.sample.system.vault.service.domain.ports.output.repository.RedisOtpRepository;
import com.sample.system.vault.service.domain.response.GenerateOtpResponse;
import com.sample.system.vault.service.domain.response.VerifyOtpResponse;
import com.sample.system.vault.service.domain.utility.SecureRandomHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.vault.VaultException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Qualifier("otpSecretService")
public class OtpServiceImpl implements SecretService<GenerateOtpRequest, GenerateOtpResponse, VerifyOtpRequest, VerifyOtpResponse> {

    private final RestTemplate restTemplate;
    private final RedisOtpRepository redisOtpRepository;
    private final CryptoRepository cryptoRepository;
    @Value("${app.vault.uri}")
    private String vaultUrl;

    @Value("${app.vault.token}")
    private String vaultToken;

    @Value("${app.vault.otpHmacKey}")
    private String otpHmacKey;

    @Value("${app.vault.otpEncryptKey}")
    private String otpEncryptKey;

    @Value("${app.vault.totpStepSeconds:120}")
    private int totpStepSeconds;

    @Value("${app.vault.totpDigits:6}")
    private int totpDigits;

    @Value("${otp.validity.seconds:115}")
    private int otpValiditySeconds;

    @Override
    public GenerateOtpResponse generate(GenerateOtpRequest request) throws VaultDomainException {
        String cardId = request.getCardId();
        log.info("Generating otp for cardId: {}", cardId);
        // Generate plain OTP
        String plainOtp = String.format("%06d", SecureRandomHolder.nextInt(1_000_000));
        log.info("Generating otp for cardId: {}, otp: {}", cardId, plainOtp);
        // Encrypt OTP
        String encryptedOtp = cryptoRepository.encrypt(otpEncryptKey, plainOtp.getBytes(StandardCharsets.UTF_8));
        log.info("Generating otp for cardId: {}, encryptedOtp: {}", cardId, encryptedOtp);
        // Generate HMAC of the OTP
        String hmacOtp = cryptoRepository.hmac(otpHmacKey, plainOtp.getBytes(StandardCharsets.UTF_8));
        log.info("OtpHmac:{} for cardId:{} generated successfully", hmacOtp, cardId);
        // Save OTP HMAC (to compare on validation later) - use cardId as key
        Instant expireAt = Instant.now().plusSeconds(otpValiditySeconds);
        redisOtpRepository.saveOtp(cardId, hmacOtp, expireAt);

        return new GenerateOtpResponse(encryptedOtp, hmacOtp, expireAt);
    }

    @Override
    public VerifyOtpResponse verify(VerifyOtpRequest request) throws VaultDomainException {
        String cardId = request.cardId();
        String providedOtp = request.providedOtp();

        log.info("Verifying otp for cardId:{}", cardId);
        byte[] decryptedPin;
        try {
            decryptedPin = cryptoRepository.decrypt(otpEncryptKey, providedOtp);
        } catch (VaultException ex) {
            throw new VaultDomainException(
                    "providedOtp is invalid",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST
            );
        }
        log.info("decrypt OTP successfully for cardId :{}", cardId);

        Instant expireAt = redisOtpRepository.getExpireAt(cardId);
        if (expireAt == null || Instant.now().isAfter(expireAt)) {
            throw new VaultDomainException("OTP expired or not generated",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }

        String expectedHmac = redisOtpRepository.getOtp(cardId);
        if (expectedHmac == null) {
            throw new VaultDomainException("OTP expired or not generated",
                    StatusService.INPUT_PARAMETER_NOT_VALID, HttpStatus.BAD_REQUEST);
        }

        String providedHmac = cryptoRepository.hmac(otpHmacKey, decryptedPin);
        boolean valid = MessageDigest.isEqual(
                expectedHmac.getBytes(StandardCharsets.UTF_8),
                providedHmac.getBytes(StandardCharsets.UTF_8)
        );

        if (valid) {
            redisOtpRepository.deleteOtp(cardId);
            log.info("Otp validation was successful for cardId: {}", cardId);
        } else {
            redisOtpRepository.incrementRetry(cardId, expireAt);
            log.info("Otp validation was failed for cardId: {}", cardId);
        }

        return new VerifyOtpResponse(valid);
    }

    private String buildKeyName(String cardId) {
        return "card-" + cardId;
    }

    private void ensureKeyExists(String keyName, String cardId) {
        log.info("ensure key exist or not for cardId: {}, keyName:{}",cardId, keyName);
        try {
            String url = vaultUrl + "/v1/otp/keys/" + keyName;

            HttpHeaders headers = vaultHeaders();HttpEntity<?> entity = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            log.info("key exist in the vault");
        } catch (Exception ex) {
            log.info("Vault key not found, creating new key: {}", keyName);
            String url = vaultUrl + "/v1/otp/keys/" + keyName;

            HttpHeaders headers = vaultHeaders();
            Map<String, Object> body = Map.of(
                    "type", "totp",
                    "digits", totpDigits,
                    "period", totpStepSeconds
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        }
    }

    private String callVaultGenerate(String keyName) {

        String url = vaultUrl + "/v1/otp/code/" + keyName;

        HttpHeaders headers = vaultHeaders();
        HttpEntity<String> entity = new HttpEntity<>("{}", headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        Map data = (Map) response.getBody().get("data");
        return (String) data.get("code");
    }

    private boolean callVaultValidate(String keyName, String code) {

        String url = vaultUrl + "/v1/otp/validate/" + keyName;

        HttpHeaders headers = vaultHeaders();

        Map<String, String> body = Map.of("code", code);

        HttpEntity<Map<String, String>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        Map data = (Map) response.getBody().get("data");

        return Boolean.TRUE.equals(data.get("valid"));
    }

    private HttpHeaders vaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Vault-Token", vaultToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}