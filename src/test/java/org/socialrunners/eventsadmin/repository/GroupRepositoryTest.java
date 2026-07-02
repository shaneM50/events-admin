package org.socialrunners.eventsadmin.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.socialrunners.eventsadmin.model.Group;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GroupRepositoryTest {

    @Autowired
    GroupRepository groupRepository;

    @Test
    void canSaveAndLoadGroup() {
        Group saved = groupRepository.save(
            new Group(
                "Admins",
                "Admin test group",
                "Seville",
                "Spain",
                true,
                "contact@example.com",
                "@admins_handle"
            )
        );

        Long id = saved.getId();
        assertThat(id).isNotNull();

        Group found = groupRepository.findById(id).orElseThrow();
        assertThat(found.getName()).isEqualTo("Admins");
        assertThat(found.getDescription()).isEqualTo("Admin test group");
        assertThat(found.getCity()).isEqualTo("Seville");
        assertThat(found.getCountry()).isEqualTo("Spain");
    }
}
