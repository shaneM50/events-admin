package org.socialrunners.eventsadmin.config;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${security.group-admin.username:group_admin}")
    private String groupAdminUsername;

    @Value("${security.group-admin.password:group_admin}")
    private String groupAdminPassword;

    @Value("${security.group-organizer.username:group_organizer}")
    private String groupOrganizerUsername;

    @Value("${security.group-organizer.password:group_organizer}")
    private String groupOrganizerPassword;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/groups/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/groups/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/groups/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/groups/**").authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * In-memory users for dev/test, with credentials configurable via env vars
     * or Spring properties:
     *
     *  - security.group-admin.username / password
     *  - security.group-organizer.username / password
     *
     * Defaults:
     *  - group_admin / group_admin
     *  - group_organizer / group_organizer
     */
    @Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails groupAdmin = User.withUsername(groupAdminUsername)
                .password(passwordEncoder.encode(groupAdminPassword))
                .roles("GROUP_ADMIN")
                .build();

        UserDetails groupOrganizer = User.withUsername(groupOrganizerUsername)
                .password(passwordEncoder.encode(groupOrganizerPassword))
                .roles("GROUP_ORGANIZER")
                .build();

        return new InMemoryUserDetailsManager(groupAdmin, groupOrganizer);
    }

    /**
     * Delegating password encoder with sensible defaults.
     * For our simple in-memory users, we just encode whatever password
     * is provided (dev/test only). For production, replace this with a
     * stronger, well-configured encoder.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
