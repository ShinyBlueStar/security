package com.sample.system.vault.service.config;

import com.sample.system.vault.service.domain.exception.VaultDomainException;
import com.sample.system.vault.service.domain.ports.input.service.StatusService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultToken;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Configuration
public class VaultConfig {

    @Bean
    public VaultEndpoint vaultEndpoint(VaultProperties props) {
        return VaultEndpoint.from(URI.create(props.uri()));
    }

    /**
     * VaultTemplate with conditional authentication:
     * - Uses Token authentication if token is provided
     * - Uses AppRole authentication if roleId and secretId are provided
     * - Throws exception if neither is configured
     */
    @Bean
    public VaultTemplate vaultTemplate(VaultEndpoint endpoint, VaultProperties props) throws VaultDomainException {
        // Try token first (simpler and more common)
        if (StringUtils.hasText(props.token())) {
            return new VaultTemplate(endpoint, new TokenAuthentication(VaultToken.of(props.token())));
        }
        
        // Fallback to AppRole if roleId and secretId are provided
        if (StringUtils.hasText(props.roleId()) && StringUtils.hasText(props.secretId())) {
            var options = AppRoleAuthenticationOptions.builder()
                    .roleId(AppRoleAuthenticationOptions.RoleId.provided(props.roleId()))
                    .secretId(AppRoleAuthenticationOptions.SecretId.provided(props.secretId()))
                    .build();
            AppRoleAuthentication appRoleAuth = new AppRoleAuthentication(options, new RestTemplate());
            return new VaultTemplate(endpoint, appRoleAuth);
        }
        
        // If neither is provided, throw a clear exception
        throw new VaultDomainException(
                "Vault authentication requires either 'app.vault.token' or both 'app.vault.roleId' and 'app.vault.secretId' to be configured. " +
                "Currently token is empty and roleId/secretId are not both provided.",
                StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
