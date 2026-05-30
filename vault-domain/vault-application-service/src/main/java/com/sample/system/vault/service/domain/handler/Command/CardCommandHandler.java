package com.sample.system.vault.service.domain.handler.Command;

import ch.qos.logback.core.util.StringUtil;
import com.sample.system.vault.service.domain.command.GenerateCvvRequest;
import com.sample.system.vault.service.domain.command.GenerateOtpRequest;
import com.sample.system.vault.service.domain.command.GeneratePin1Request;
import com.sample.system.vault.service.domain.exception.VaultDomainException;
import com.sample.system.vault.service.domain.ports.input.service.SecretService;
import com.sample.system.vault.service.domain.ports.input.service.SessionService;
import com.sample.system.vault.service.domain.response.GenerateCvvResponse;
import com.sample.system.vault.service.domain.response.GenerateOtpResponse;
import com.sample.system.vault.service.domain.response.GeneratePinResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardCommandHandler {
    private final SessionService sessionService;
    @Qualifier("pinSecretService")
    private final SecretService<GeneratePin1Request, GeneratePinResponse, ?, ?> pinSecretService;
    @Qualifier("otpSecretService")
    private final SecretService<GenerateOtpRequest, GenerateOtpResponse, ?, ?> otpSecretService;
    @Qualifier("cvvSecretService")
    private final SecretService<GenerateCvvRequest, GenerateCvvResponse, ?, ?> cvvSecretService;

    public GeneratePinResponse generatePin1(GeneratePin1Request request) throws VaultDomainException {
        log.info("generatePin1 for cardId: {}", request.getCardId());
        return pinSecretService.generate(request);
    }

    public GenerateOtpResponse generateOtp(GenerateOtpRequest request) throws VaultDomainException {
        log.info("generate Otp for cardId: {}", request.getCardId());
        GenerateOtpResponse result = otpSecretService.generate(request);
        log.info("generate Otp successfully");
        return result;
    }

    public GenerateCvvResponse generateCvv2(GenerateCvvRequest request) throws VaultDomainException {
        log.info("generate CVV2 for cardId: {}", request.getCardId());
        GenerateCvvResponse result = cvvSecretService.generate(request);
        log.info("generate CVV2 successfully");
        return result;
    }
}
