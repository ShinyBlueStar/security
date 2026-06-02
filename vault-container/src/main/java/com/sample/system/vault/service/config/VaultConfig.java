package com.sample.system.vault.service.config;

import com.sample.system.vault.service.domain.exception.VaultDomainException;
import com.sample.system.vault.service.domain.ports.input.service.StatusService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StringUtils;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.LifecycleAwareSessionManager;
import org.springframework.vault.authentication.SessionManager;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.ClientHttpRequestFactoryFactory;
import org.springframework.vault.client.VaultClients;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.ClientOptions;
import org.springframework.vault.support.SslConfiguration;
import org.springframework.vault.support.VaultToken;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Configuration
public class VaultConfig {

    @Bean
    public VaultEndpoint vaultEndpoint(VaultProperties props) {
        return VaultEndpoint.from(URI.create(props.uri()));
    }

    /**
     * Token authentication if a token is configured, AppRole otherwise. The resulting
     * ClientAuthentication is handed to a LifecycleAwareSessionManager (see vaultSessionManager)
     * which renews or re-authenticates the session automatically.
     */
    @Bean
    public ClientAuthentication clientAuthentication(VaultProperties props) throws VaultDomainException {
        if (StringUtils.hasText(props.token())) {
            return new TokenAuthentication(VaultToken.of(props.token()));
        }

        if (StringUtils.hasText(props.roleId()) && StringUtils.hasText(props.secretId())) {
            var options = AppRoleAuthenticationOptions.builder()
                    .roleId(AppRoleAuthenticationOptions.RoleId.provided(props.roleId()))
                    .secretId(AppRoleAuthenticationOptions.SecretId.provided(props.secretId()))
                    .build();
            return new AppRoleAuthentication(options, new RestTemplate());
        }

        throw new VaultDomainException(
                "Vault authentication requires either 'app.vault.token' or both 'app.vault.roleId' and 'app.vault.secretId' to be configured. " +
                "Currently token is empty and roleId/secretId are not both provided.",
                StatusService.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @Bean
    public TaskScheduler vaultTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("vault-session-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public ClientHttpRequestFactory vaultClientHttpRequestFactory() {
        return ClientHttpRequestFactoryFactory.create(new ClientOptions(), SslConfiguration.unconfigured());
    }

    @Bean
    public SessionManager vaultSessionManager(ClientAuthentication clientAuthentication,
                                               VaultEndpoint endpoint,
                                               ClientHttpRequestFactory requestFactory,
                                               TaskScheduler taskScheduler) {
        RestOperations restOperations = VaultClients.createRestTemplate(endpoint, requestFactory);
        return new LifecycleAwareSessionManager(clientAuthentication, taskScheduler, restOperations);
    }

    @Bean
    public VaultTemplate vaultTemplate(VaultEndpoint endpoint,
                                        ClientHttpRequestFactory requestFactory,
                                        SessionManager sessionManager) {
        return new VaultTemplate(endpoint, requestFactory, sessionManager);
    }

    @Bean
    public CircuitBreaker vaultCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("vault");
    }

    @Bean
    public Retry vaultRetry(RetryRegistry registry) {
        return registry.retry("vault");
    }

    /**
     * The bean application code should inject. Wraps VaultTemplate with the "vault"
     * circuit breaker + retry configured in application.yml.
     */
    @Bean
    @Primary
    public VaultOperations vaultOperations(VaultTemplate vaultTemplate, CircuitBreaker vaultCircuitBreaker, Retry vaultRetry) {
        return ResilientVaultOperations.wrap(vaultTemplate, vaultCircuitBreaker, vaultRetry);
    }
}
