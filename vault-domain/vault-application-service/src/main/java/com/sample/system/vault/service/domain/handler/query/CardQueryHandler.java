package com.sample.system.vault.service.domain.handler.query;

import com.sample.system.vault.service.domain.command.ValidatePinRequest;
import com.sample.system.vault.service.domain.command.VerifyCvvRequest;
import com.sample.system.vault.service.domain.command.VerifyOtpRequest;
import com.sample.system.vault.service.domain.exception.VaultDomainException;
import com.sample.system.vault.service.domain.ports.input.service.SecretService;
import com.sample.system.vault.service.domain.response.ValidatePinResponse;
import com.sample.system.vault.service.domain.response.VerifyCvvResponse;
import com.sample.system.vault.service.domain.response.VerifyOtpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardQueryHandler {
    @Qualifier("pinSecretService")
    private final SecretService<?, ?, ValidatePinRequest, ValidatePinResponse> pinSecretService;
    @Qualifier("cvvSecretService")
    private final SecretService<?, ?, VerifyCvvRequest, VerifyCvvResponse> cvvSecretService;
    @Qualifier("otpSecretService")
    private final SecretService<?, ?, VerifyOtpRequest, VerifyOtpResponse> otpSecretService;

    public ValidatePinResponse validatePin1(ValidatePinRequest request)
            throws VaultDomainException {
        log.info("Verifying PIN for cardId: {}", request.cardId());
        return pinSecretService.verify(request);
    }

    public VerifyCvvResponse verifyCvv(VerifyCvvRequest request) throws VaultDomainException {
        log.info("verify cvv2 for cardId: {}", request.cardId());
        return cvvSecretService.verify(request);
    }

    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) throws VaultDomainException {
        log.info("verify otp for cardId: {}", request.cardId());
        return otpSecretService.verify(request);
    }
}
