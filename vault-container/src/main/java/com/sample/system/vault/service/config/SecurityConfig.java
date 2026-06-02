package com.sample.system.vault.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Requires a valid JWT bearer token on every request, except Swagger/OpenAPI,
 * actuator health/info, and (outside production) the H2 console.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/health/**",
            "/actuator/info"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // H2 console renders itself in a frame; it's disabled outright in application-production.yml.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(VaultSecurityResponses.unauthorized())
                        .accessDeniedHandler(VaultSecurityResponses.forbidden()))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                        .authenticationEntryPoint(VaultSecurityResponses.unauthorized()));

        return http.build();
    }
}
