package org.socialrunners.eventsadmin.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test-only security configuration.
 *
 * When we add spring-boot-starter-security, Spring auto-registers a security filter chain
 * that requires authentication, which makes all @WebMvcTest requests return 401/403 by default.
 *
 * This test config replaces that behaviour for slice tests by permitting all requests,
 * so controller tests can focus on controller logic (status codes, JSON, paging, etc.)
 * without needing a real Authentication or IdP setup.
 *
 * In production, the main SecurityConfig is used instead.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
