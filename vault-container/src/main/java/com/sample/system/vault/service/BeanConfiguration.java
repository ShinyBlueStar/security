package com.sample.system.vault.service;

import com.sample.system.vault.service.domain.VaultDomainService;
import com.sample.system.vault.service.domain.VaultDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfiguration {

    @Bean
    public VaultDomainService vaultDomainService() {
        return new VaultDomainServiceImpl();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}