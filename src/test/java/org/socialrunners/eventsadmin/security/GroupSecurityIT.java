package org.socialrunners.eventsadmin.security;

import org.junit.jupiter.api.Test;
import org.socialrunners.eventsadmin.model.Group;
import org.socialrunners.eventsadmin.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc 
/**
 * Integration tests for security behavior on the /groups endpoints.
 *
 * This class loads the full Spring Boot application context and uses the real
 * security configuration from {@link org.socialrunners.eventsadmin.config.SecurityConfig}
 * (via @SpringBootTest + @AutoConfigureMockMvc).
 *
 * Unlike the controller slice tests, no TestSecurityConfig
 * is imported here, so the actual security filters and method-level rules are
 * applied. These tests verify which roles can and cannot call POST /groups.
 */
class GroupSecurityIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    GroupRepository groupRepository;

    @Test
    @WithMockUser(roles = "GROUP_ADMIN")
    void postGroups_shouldReturnCreatedForGroupAdmin() throws Exception {
        Group newGroup = new Group("NYC Runners");
        Group savedGroup = new Group("NYC Runners");

        String body = objectMapper.writeValueAsString(newGroup);

        given(groupRepository.save(any(Group.class))).willReturn(savedGroup);

        mvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "GROUP_USER")
    void postGroups_shouldReturnForbiddenForNonAdmin() throws Exception {
        Group newGroup = new Group("NYC Runners");
        String body = objectMapper.writeValueAsString(newGroup);

        mvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());
    }

    @Test
    void postGroups_shouldReturnForbiddenForAnonymous() throws Exception {
        Group newGroup = new Group("NYC Runners");
        String body = objectMapper.writeValueAsString(newGroup);

        mvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());
    }
}
