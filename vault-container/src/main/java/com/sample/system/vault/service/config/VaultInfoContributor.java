package com.sample.system.vault.service.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
public class VaultInfoContributor implements InfoContributor {
    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("app",
                Collections.unmodifiableMap(
                        Map.of(
                                "name", "سرویس مشتریان vault-service",
                                "version", "1.0.0",
                                "description", "سلام من زنده هستم و کامل تونستم به سرور دیسکاوری وصل بشم"
                        )
                )
        );
    }
}
