package com.sample.system.vault.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.sample.system.vault.service.config.VaultProperties;

@EnableJpaRepositories(basePackages = { "com.sample.system.vault.infra.repository" })
@EntityScan(basePackages = { "com.sample.system.vault.infra.entity" })
@SpringBootApplication(scanBasePackages = {
    "com.sample.system.vault",
    "com.sample.system.vault.service",
    "com.sample.system.vault.infra.adapter",
    "com.sample.system.vault.infra.mapper",
    "com.sample.system.vault.service.application.rest"
})
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(VaultProperties.class)
public class VaultServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VaultServiceApplication.class, args);
    }
}
