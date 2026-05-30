package com.sample.system.vault.service.domain.ports.input.service.impl;

import com.sample.system.vault.service.domain.command.GeneratePin1Request;
import com.sample.system.vault.service.domain.command.ValidatePinRequest;
import com.sample.system.vault.service.domain.exception.VaultDomainException;
import com.sample.system.vault.service.domain.ports.input.service.SecretService;
import com.sample.system.vault.service.domain.ports.input.service.StatusService;
import com.sample.system.vault.service.domain.ports.output.repository.CryptoRepository;
import com.sample.system.vault.service.domain.response.GeneratePinResponse;
import com.sample.system.vault.service.domain.response.ValidatePinResponse;
import com.sample.system.vault.service.domain.utility.SecureRandomHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Service
@Qualifier("pinSecretService")
public class PinServiceImpl implements SecretService<GeneratePin1Request, GeneratePinResponse, ValidatePinRequest, ValidatePinResponse> {

    private final CryptoRepository cryptoRepository;
    @Value("${app.vault.pinKey}")
    private String pinKey;
    @Value("${app.vault.pinEncryptKey}")
    private String pinEncryptKey;

    public PinServiceImpl(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    @Override
    public GeneratePinResponse generate(GeneratePin1Request request) throws VaultDomainException {
        String cardId = request.getCardId();
        log.info("generate pin for cardId:{}", cardId);
        try {
            String pin = String.format("%04d", SecureRandomHolder.nextInt(10000));
            log.info("pin:{} generated for cardId:{}", pin, cardId);
            String encryptedPin = cryptoRepository.encrypt(pinEncryptKey, pin.getBytes(StandardCharsets.UTF_8));
            log.info("encryptedPin:{} for card: {}", encryptedPin, cardId);
            String pinHmac = cryptoRepository.hmac(pinKey, pin.getBytes(StandardCharsets.UTF_8));
            log.info("pinHmac:{} for cardId:{} generated successfully", pinHmac, cardId);
            return new GeneratePinResponse(encryptedPin, pinHmac);
        } catch (Exception e) {
            throw new VaultDomainException("PIN generation failed: " + e.getMessage(),
                    StatusService.PIN_GENERATION_ERROR, HttpStatus.OK);
        }
    }

    @Override
    public ValidatePinResponse verify(ValidatePinRequest request) throws VaultDomainException {
        String cardId = request.cardId();
        String hashedPin = request.hashedPin();
        String providedPin = request.providedPin();

        log.info("validate pin for cardId:{}, pin:{}, providedPIn:{}", cardId, hashedPin, providedPin);
        try {
            // Decrypt the provided PIN and compute its HMAC, then compare with hashedPin
            byte[] decryptedPin = cryptoRepository.decrypt(pinEncryptKey, providedPin);
            String computedHmac = cryptoRepository.hmac(pinKey, decryptedPin);
            log.info("computedHmac:{}", computedHmac);
            boolean valid = MessageDigest.isEqual(hashedPin.getBytes(StandardCharsets.UTF_8),
                    computedHmac.getBytes(StandardCharsets.UTF_8));
            if (!valid) {
                log.error("Invalid pin");
            }
            return new ValidatePinResponse(valid);
        } catch (Exception e) {
            log.error("PIN validation failed for cardId {}: {}", cardId, e.getMessage(), e);
            throw new VaultDomainException("PIN validation failed: " + e.getMessage(),
                    StatusService.PIN_IS_INVALID, HttpStatus.BAD_REQUEST);
        }
    }

}

