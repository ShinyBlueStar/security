package com.sample.system.vault.service.domain.handler.Command;

import com.sample.system.vault.service.domain.ports.input.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SessionCommandHandler {
    private final SessionService sessionService;

}
