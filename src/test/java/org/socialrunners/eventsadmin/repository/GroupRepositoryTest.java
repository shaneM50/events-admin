package org.socialrunners.eventsadmin.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.socialrunners.eventsadmin.model.Group;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GroupRepositoryTest {

    @Autowired
    GroupRepository groupRepository;

    @Test
    void canSaveAndLoadGroup() {
        Group saved = groupRepository.save(new Group("Admins"));
        Long id = saved.getId();
        assertThat(id).isNotNull();

        Group found = groupRepository.findById(id).orElseThrow();
        assertThat(found.getName()).isEqualTo("Admins");
    }
}
