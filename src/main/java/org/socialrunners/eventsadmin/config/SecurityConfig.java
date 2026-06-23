package org.socialrunners.eventsadmin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * HTTP security configuration.
     *
     * - Allows unauthenticated GETs to /groups/** (public read).
     * - Requires authentication for POST /groups/**.
     * - Uses HTTP Basic auth for development so we can easily test
     *   credentials with tools like MockMvc and Postman.
     *
     * Method-level rules (@PreAuthorize) still apply on top of this
     * (e.g. only GROUP_ADMIN can create groups).
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/groups/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/groups/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/groups/**").authenticated()
                .anyRequest().permitAll()
            )
            .httpBasic(Customizer.withDefaults()); // Basic Auth for dev/testing

        return http.build();
    }

    /**
     * In-memory users for local development and tests.
     *
     * This avoids having to set up a real user store or IdP while we build
     * out the API. Credentials are:
     *  - group_admin / group_admin   → role GROUP_ADMIN
     *  - group_organizer / group_organizer → role GROUP_USER
     *
     * These users are used by:
     *  - HTTP Basic auth when calling the API directly (e.g. via Postman),
     *  - @WithMockUser-based tests that assert role behavior.
     *
     * In a real environment, this should be replaced with a proper user
     * directory or external IdP.
     */
    @Bean
    UserDetailsService userDetailsService() {
        UserDetails groupAdmin = User.withUsername("group_admin")
                .password("{noop}group_admin")      
                .roles("GROUP_ADMIN")
                .build();

        UserDetails groupOrganizer = User.withUsername("group_organizer")
                .password("{noop}group_organizer")  
                .roles("GROUP_ORGANIZER")
                .build();

        return new InMemoryUserDetailsManager(groupAdmin, groupOrganizer);
    }


    /**
     * Password encoder for dev-only in-memory users.
     *
     * Uses a DelegatingPasswordEncoder with {noop} for our test users so we
     * avoid the deprecated NoOpPasswordEncoder but still have simple plaintext
     * passwords in dev. In production, switch to strong encoders like bcrypt.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
