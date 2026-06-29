package org.socialrunners.eventsadmin.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.socialrunners.eventsadmin.model.AppUser;
import org.socialrunners.eventsadmin.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class AppUserRepositoryTest {

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    RoleRepository roleRepository;

    PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Test
    @DisplayName("findByUsername should return user when username exists")
    void findByUsername_existingUser() {
        String rawPassword = "secret";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Role adminRole = new Role("GROUP_ADMIN", "Full administration over groups");
        roleRepository.saveAndFlush(adminRole);

        AppUser user = new AppUser(
            "group_admin",
            encodedPassword,
            Set.of(adminRole)
        );
        appUserRepository.saveAndFlush(user);

        Optional<AppUser> result = appUserRepository.findByUsername("group_admin");

        assertThat(result).isPresent();
        AppUser found = result.get();
        assertThat(found.getUsername()).isEqualTo("group_admin");
        assertThat(passwordEncoder.matches(rawPassword, found.getPassword())).isTrue();
        assertThat(found.getRoles())
            .extracting(Role::getName)
            .containsExactly("GROUP_ADMIN");
    }

    @Test
    @DisplayName("findByUsername should return empty when username does not exist")
    void findByUsername_nonExistingUser() {
        Optional<AppUser> result = appUserRepository.findByUsername("does_not_exist");
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("username should be unique")
    void username_shouldBeUnique() {
        String encodedPassword = passwordEncoder.encode("secret");

        Role adminRole = new Role("GROUP_ADMIN", "Full administration over groups");
        Role organizerRole = new Role("GROUP_ORGANIZER", "Can manage events but not delete groups");

        // Persist roles first so they have IDs
        roleRepository.saveAndFlush(adminRole);
        roleRepository.saveAndFlush(organizerRole);

        AppUser first = new AppUser(
            "duplicate_user",
            encodedPassword,
            Set.of(adminRole)
        );
        appUserRepository.saveAndFlush(first);

        AppUser second = new AppUser(
            "duplicate_user", // same username
            encodedPassword,
            Set.of(organizerRole)
        );

        assertThatThrownBy(() -> appUserRepository.saveAndFlush(second))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
