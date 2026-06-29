package org.socialrunners.eventsadmin.config;

import org.socialrunners.eventsadmin.model.AppUser;
import org.socialrunners.eventsadmin.model.Role;
import org.socialrunners.eventsadmin.repository.AppUserRepository;
import org.socialrunners.eventsadmin.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Bootstrap initializer that guarantees a system administrator user exists.
 *
 * <p>On every application startup it:
 * <ul>
 *   <li>Ensures the {@code GROUP_ADMIN} role exists (creates it if missing).</li>
 *   <li>Ensures a {@code security.group-admin.username} user exists (creates it if missing),
 *       encoding the plain-text password from {@code security.group-admin.password} using the
 *       configured {@link PasswordEncoder} and assigning the {@code GROUP_ADMIN} role.</li>
 * </ul>
 *
 * <p>This makes it safe to deploy to a fresh database and still have a guaranteed way to log
 * into the system as an administrator, while keeping the actual credentials externalised in
 * configuration or environment variables.</p>
 */
@Configuration
public class AdminUserInitializer {

    @Value("${security.group-admin.username:group_admin}")
    private String adminUsername;

    @Value("${security.group-admin.password:group_admin}")
    private String adminPassword;

    @Bean
    CommandLineRunner ensureAdminUser(RoleRepository roleRepository,
                                      AppUserRepository userRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            Role adminRole = roleRepository.findByName("GROUP_ADMIN")
                    .orElseGet(() -> roleRepository.save(
                            new Role("GROUP_ADMIN", "Full administration over groups")
                    ));

            userRepository.findByUsername(adminUsername).orElseGet(() -> {
                AppUser admin = new AppUser(
                        adminUsername,
                        passwordEncoder.encode(adminPassword),
                        Set.of(adminRole)
                );
                return userRepository.save(admin);
            });
        };
    }
}
