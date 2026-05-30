package com.sample.system.vault.service.domain.ports.input.service.impl;

import com.sample.system.vault.service.domain.command.GenerateCvvRequest;
import com.sample.system.vault.service.domain.command.VerifyCvvRequest;
import com.sample.system.vault.service.domain.exception.VaultDomainException;
import com.sample.system.vault.service.domain.ports.input.service.SecretService;
import com.sample.system.vault.service.domain.ports.input.service.StatusService;
import com.sample.system.vault.service.domain.ports.output.repository.CryptoRepository;
import com.sample.system.vault.service.domain.response.GenerateCvvResponse;
import com.sample.system.vault.service.domain.response.VerifyCvvResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.vault.VaultException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
@Qualifier("cvvSecretService")
public class CvvServiceImpl implements SecretService<GenerateCvvRequest, GenerateCvvResponse, VerifyCvvRequest, VerifyCvvResponse> {

    private final CryptoRepository cryptoRepository;
    @Value("${app.vault.cvv2Key}")
    private String cvv2Key;

    @Value("${app.vault.cvv2EncryptKey}")
    private String cvv2EncryptKey;

    @Value("${app.vault.cvv2Length:4}")
    private int cvv2Length;

    @Override
    public GenerateCvvResponse generate(GenerateCvvRequest request) throws VaultDomainException {
        String cardId = request.getCardId();
        String encryptedData = request.getEncryptedData();

        log.info("generate CVV2 for data: {}, cardId: {}", encryptedData, cardId);
        try {
            byte[] decryptedData = cryptoRepository.decrypt(cvv2EncryptKey, encryptedData);
            String hmacCvv2 = cryptoRepository.hmac(cvv2Key, decryptedData);
            log.info("creating hash for cvv2, hmacCvv2: {}", hmacCvv2);
            String cvv2 = deriveCvvFromCmac(hmacCvv2);
            String encryptedCvv2 = cryptoRepository.encrypt(cvv2EncryptKey, cvv2.getBytes(StandardCharsets.UTF_8));
            log.info("CVV2: {} successfully generated for cardId: {}", cvv2, cardId);
            return new GenerateCvvResponse(encryptedCvv2, hmacCvv2);
        } catch (Exception e) {
            log.error("CVV2 generation failed for CardId {}", cardId);
            throw new VaultDomainException("CVV2 generation failed", StatusService.CVV2_GENERATION_ERROR, HttpStatus.OK);
        }
    }

    @Override
    public VerifyCvvResponse verify(VerifyCvvRequest request) throws VaultDomainException {
        final var cardId = request.cardId();
        final var encryptedData = request.encryptedData();
        final var hashedCvv = request.hashedCvv();

        log.info("Verifying CVV2 for cardId: {}", cardId);

        final byte[] decryptedData;
        try {
            decryptedData = cryptoRepository.decrypt(cvv2EncryptKey, encryptedData);
        } catch (VaultException ex) {
            throw new VaultDomainException(
                    "encryptedData is invalid",
                    StatusService.INPUT_PARAMETER_NOT_VALID,
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            final var plainData = new String(decryptedData, StandardCharsets.UTF_8).trim();

            final var computedHashCvv = cryptoRepository.hmac(cvv2Key, decryptedData);
            log.info("hash value created successfully for cardId : {}, hash: {}", cardId, computedHashCvv);

            final var expectedCvvStr = deriveCvvFromCmac(hashedCvv).trim();

            final int expectedCvv;
            final int providedCvv;
            try {
                expectedCvv = Integer.parseInt(expectedCvvStr);
                providedCvv = Integer.parseInt(plainData);
            } catch (NumberFormatException ex) {
                throw new VaultDomainException(
                        "CVV2 format is invalid",
                        StatusService.INPUT_PARAMETER_NOT_VALID,
                        HttpStatus.BAD_REQUEST
                );
            }

            final boolean valid = expectedCvv == providedCvv;
            log.info("CVV2 verification for cardId: {} is {}", cardId, valid ? "succeeded" : "failed");
            return new VerifyCvvResponse(valid);
        } catch (VaultDomainException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("CVV2 verification failed for cardId {}: {}", cardId, ex.getMessage(), ex);
            throw new VaultDomainException(
                    "CVV2 validation failed",
                    StatusService.CVV2_VALIDATION_ERROR,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * EMV / ISO-style CVV derivation
     */
    private String deriveCvvFromCmac(String vaultHmacOutput) throws VaultDomainException {
        log.info("Deriving CVV2 using functional pipeline");

        String base64 = Optional.ofNullable(vaultHmacOutput)
                .map(this::extractBase64)
                .orElseThrow(() -> error("CVV2 derivation failed"));
        byte[] decoded = decodeBase64(base64);
        String hex = bytesToHex(decoded);
        int cvv = deriveCvvFromHex(hex);
        return formatCvv(cvv);
    }

    /** vault:v1:Base64String -> Base64String */
    private String extractBase64(String vaultOutput) {
        return Optional.of(vaultOutput.lastIndexOf(':'))
                .filter(i -> i >= 0)
                .map(i -> vaultOutput.substring(i + 1))
                .orElse(vaultOutput)
                .trim();
    }

    /** Base64String -> byte[] */
    private byte[] decodeBase64(String base64) throws VaultDomainException {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            if (decoded.length == 0) {
                throw error("Decoded HMAC bytes are empty");
            }
            return decoded;
        } catch (IllegalArgumentException e) {
            throw error("Invalid Base64 format in HMAC output");
        }
    }

    /** byte[] -> hex string */
    private String bytesToHex(byte[] bytes) {
        return IntStream.range(0, bytes.length)
                .mapToObj(i -> String.format("%02x", bytes[i]))
                .collect(Collectors.joining());
    }

    /** hex -> CVV integer */
    private int deriveCvvFromHex(String hex) throws VaultDomainException{
        validateHex(hex);

        int offset = calculateOffset(hex);
        long binary = extractBinary(hex, offset);

        return switch (cvv2Length) {
            case 3 -> 100 + (int) (binary % 900);
            case 4 -> 1000 + (int) (binary % 9000);
            default -> {
                log.warn("Invalid CVV2 length {}, defaulting to 3", cvv2Length);
                cvv2Length = 3;
                yield 100 + (int) (binary % 900);
            }
        };
    }

    /** formatting is last step */
    private String formatCvv(int cvv) throws VaultDomainException{
        return String.format("%0" + cvv2Length + "d", cvv);
    }

    private void validateHex(String hex) throws VaultDomainException {
        if (hex == null || hex.length() < 2) {
            throw error("Hex string too short");
        }
    }

    private int calculateOffset(String hex) {
        return Character.digit(hex.charAt(hex.length() - 1), 16) & 0x0F;
    }

    private long extractBinary(String hex, int offset) throws VaultDomainException{
        int start = offset * 2;
        int end = Math.min(start + 8, hex.length());

        if (start >= hex.length()) {
            throw error("Offset out of bounds for hex string");
        }

        return Long.parseLong(hex.substring(start, end), 16);
    }

    private VaultDomainException error(String message) {
        return new VaultDomainException(
                message,
                StatusService.CONVERT_TO_CVV2_ERROR,
                HttpStatus.OK
        );
    }

    /**
     * Constant-time comparison
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
