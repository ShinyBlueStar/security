package com.sample.system.vault.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;

@Configuration
//@EnableWebSecurity
public class SecurityConfig {

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        // *** Swagger و OpenAPI را آزاد کنید ***
//                        .requestMatchers(
//                                "/swagger-ui/**",
//                                "/swagger-ui.html",
//                                "/v3/api-docs/**",
//                                "/swagger-resources/**",
//                                "/webjars/**"
//                        ).permitAll()
//                        // *** سایر endpointها نیاز به لاگین دارند ***
//                        .anyRequest().authenticated()
//                )
//                // لاگین پیش‌فرض
//                .formLogin(form -> form.loginPage("/login").permitAll())
//                // لاگ‌اوت
//                .logout(logout -> logout.logoutSuccessUrl("/login?logout"));
//
//        return http.build();
//    }
}