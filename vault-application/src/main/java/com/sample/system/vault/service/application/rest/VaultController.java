package com.sample.system.vault.service.application.rest;

import com.sample.system.vault.service.domain.command.*;
import com.sample.system.vault.service.domain.handler.Command.CardCommandHandler;
import com.sample.system.vault.service.domain.handler.query.CardQueryHandler;
import com.sample.system.vault.service.domain.response.*;
import com.sample.system.vault.service.domain.response.base.BaseResponse;
import com.sample.system.vault.service.domain.exception.VaultDomainException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/vault", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
@Validated
@Tag(name = "Vault Operations", description = "API for PIN, OTP, and CVV operations")
public class VaultController {

    private final CardQueryHandler cardQueryHandler;
    private final CardCommandHandler cardCommandHandler;

    @PostMapping("/pin/generate")
    @Operation(
            security = {@SecurityRequirement(name = "bearer-key")},
            summary = "Generate PIN1",
            description = "Generate PIN1 for the cardId"
    )
    public ResponseEntity<BaseResponse<GeneratePinResponse>> generatePin(
            @RequestBody @Valid GeneratePin1Request request) throws VaultDomainException {
        log.info("Generating PIN for cardId: {}", request.getCardId());
        GeneratePinResponse response = cardCommandHandler.generatePin1(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse<>(true, response));
    }

    @PostMapping("/pin/verify")
    @Operation(
        security = {@SecurityRequirement(name = "bearer-key")},
        summary = "Validate PIN1",
        description = "Validate PIN1 using encrypted PIN from database"
    )
    public ResponseEntity<BaseResponse<ValidatePinResponse>> verifyPin(
            @RequestBody @Valid ValidatePinRequest request) throws VaultDomainException {
        log.info("Verifying PIN for cardId: {}", request.cardId());
        ValidatePinResponse response = cardQueryHandler.validatePin1(request);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @PostMapping("/otp/generate")
    @Operation(
        security = {@SecurityRequirement(name = "bearer-key")},
        summary = "Generate OTP",
        description = "Generate OTP (PIN2) for the cardId"
    )
    public ResponseEntity<BaseResponse<GenerateOtpResponse>> generateOtp(@RequestBody @Valid GenerateOtpRequest request)
            throws VaultDomainException {
        log.info("Generating OTP for cardId: {}", request.getCardId());
        GenerateOtpResponse response = cardCommandHandler.generateOtp(request);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @PostMapping("/otp/verify")
    @Operation(
        security = {@SecurityRequirement(name = "bearer-key")},
        summary = "Verify OTP",
        description = "Verify OTP (PIN2) for the cardId"
    )
    public ResponseEntity<BaseResponse<VerifyOtpResponse>> verifyOtp(
            @RequestBody @Valid VerifyOtpRequest request) throws VaultDomainException {
        log.info("Verifying OTP for cardId: {}", request.cardId());
        VerifyOtpResponse response = cardQueryHandler.verifyOtp(request);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @PostMapping("/cvv/generate")
    @Operation(
        security = {@SecurityRequirement(name = "bearer-key")},
        summary = "Generate CVV2",
        description = "Generate CVV2 using card data"
    )
    public ResponseEntity<BaseResponse<GenerateCvvResponse>> generateCvv(
            @RequestBody @Valid GenerateCvvRequest request) throws VaultDomainException {
        log.info("Generating CVV2 for cardId: {}", request.getCardId());
        GenerateCvvResponse response = cardCommandHandler.generateCvv2(request);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @PostMapping("/cvv/verify")
    @Operation(
            security = {@SecurityRequirement(name = "bearer-key")},
            summary = "Verify CVV2",
            description = "Verify CVV2 using card data"
    )
    public ResponseEntity<BaseResponse<VerifyCvvResponse>> verifyCvv(
            @RequestBody @Valid VerifyCvvRequest request) throws VaultDomainException {
        log.info("Verifying CVV for cardId: {}", request.cardId());
        VerifyCvvResponse response = cardQueryHandler.verifyCvv(request);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }
}