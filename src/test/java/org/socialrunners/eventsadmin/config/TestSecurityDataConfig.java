package org.socialrunners.eventsadmin.config;

import org.socialrunners.eventsadmin.model.AppUser;
import org.socialrunners.eventsadmin.model.Role;
import org.socialrunners.eventsadmin.repository.AppUserRepository;
import org.socialrunners.eventsadmin.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@TestConfiguration
public class TestSecurityDataConfig {

    @Bean
    CommandLineRunner seedTestUsersAndRoles(RoleRepository roleRepository,
                                            AppUserRepository userRepository,
                                            PasswordEncoder passwordEncoder) {
        return args -> {
            Role adminRole = roleRepository.findByName("GROUP_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role(
                            "GROUP_ADMIN",
                            "Full administration over groups"
                    )));

            Role organizerRole = roleRepository.findByName("GROUP_ORGANIZER")
                    .orElseGet(() -> roleRepository.save(new Role(
                            "GROUP_ORGANIZER",
                            "Can manage events but not delete groups"
                    )));

            userRepository.findByUsername("group_admin").orElseGet(() -> {
                AppUser user = new AppUser(
                        "group_admin",
                        passwordEncoder.encode("group_admin"),
                        Set.of(adminRole)
                );
                return userRepository.save(user);
            });

            userRepository.findByUsername("group_organizer").orElseGet(() -> {
                AppUser user = new AppUser(
                        "group_organizer",
                        passwordEncoder.encode("group_organizer"),
                        Set.of(organizerRole)
                );
                return userRepository.save(user);
            });
        };
    }
}
