package org.socialrunners.eventsadmin.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.socialrunners.eventsadmin.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    RoleRepository roleRepository;

    @Test
    @DisplayName("findByName should return role when name exists")
    void findByName_existingRole() {
        // arrange
        Role role = new Role("GROUP_ADMIN", "Full administration over groups");
        roleRepository.saveAndFlush(role);

        // act
        Optional<Role> result = roleRepository.findByName("GROUP_ADMIN");

        // assert
        assertThat(result).isPresent();
        Role found = result.get();
        assertThat(found.getName()).isEqualTo("GROUP_ADMIN");
        assertThat(found.getDescription()).isEqualTo("Full administration over groups");
    }

    @Test
    @DisplayName("findByName should return empty when name does not exist")
    void findByName_nonExistingRole() {
        Optional<Role> result = roleRepository.findByName("DOES_NOT_EXIST");
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("role name should be unique")
    void roleName_shouldBeUnique() {
        // arrange
        Role first = new Role("GROUP_ORGANIZER", "Can manage events but not delete groups");
        roleRepository.saveAndFlush(first);

        Role second = new Role("GROUP_ORGANIZER", "Another description");

        // act + assert
        assertThatThrownBy(() -> roleRepository.saveAndFlush(second))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
