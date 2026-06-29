package org.socialrunners.eventsadmin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.socialrunners.eventsadmin.config.TestSecurityDataConfig;
import org.socialrunners.eventsadmin.model.Group;
import org.socialrunners.eventsadmin.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityDataConfig.class)
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
    @WithMockUser(roles = "GROUP_ORGANIZER")
    void postGroups_shouldReturnForbiddenForNonAdmin() throws Exception {
        Group newGroup = new Group("NYC Runners");
        String body = objectMapper.writeValueAsString(newGroup);

        mvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());
    }

    @Test
    void postGroups_shouldReturnUnauthorizedForAnonymous() throws Exception {
        Group newGroup = new Group("NYC Runners");
        String body = objectMapper.writeValueAsString(newGroup);

        mvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void postGroups_withGroupAdminBasicAuth_shouldReturnCreated() throws Exception {
        Group newGroup = new Group("NYC Runners");
        Group savedGroup = new Group("NYC Runners");
        String body = objectMapper.writeValueAsString(newGroup);

        given(groupRepository.save(any(Group.class))).willReturn(savedGroup);

        mvc.perform(post("/groups")
                .with(httpBasic("group_admin", "group_admin"))  
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());
    }

    @Test
    void postGroups_withGroupOrganizerBasicAuth_shouldReturnForbidden() throws Exception {
        Group newGroup = new Group("NYC Runners");
        String body = objectMapper.writeValueAsString(newGroup);

        mvc.perform(post("/groups")
                .with(httpBasic("group_organizer", "group_organizer")) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());
    }

    @Test
    void postGroups_withInvalidBasicAuth_shouldReturnUnauthorized() throws Exception {
        Group newGroup = new Group("NYC Runners");
        String body = objectMapper.writeValueAsString(newGroup);

        mvc.perform(post("/groups")
                .with(httpBasic("wrong", "creds"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "GROUP_ADMIN")
    void deleteGroup_shouldReturnNoContentForGroupAdmin() throws Exception {
        long id = 42L;
        given(groupRepository.existsById(id)).willReturn(true);

        mvc.perform(delete("/groups/{id}", id))
        .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "GROUP_ORGANIZER")
    void deleteGroup_shouldReturnForbiddenForNonAdmin() throws Exception {
        long id = 42L;
        given(groupRepository.existsById(id)).willReturn(true);

        mvc.perform(delete("/groups/{id}", id))
        .andExpect(status().isForbidden());
    }

    @Test
    void deleteGroup_shouldReturnUnauthorizedForAnonymous() throws Exception {
        long id = 42L;
        // No need to stub existsById here: request never reaches controller when unauthorized.

        mvc.perform(delete("/groups/{id}", id))
        .andExpect(status().isUnauthorized());
    }

    // Basic Auth tests

    @Test
    void deleteGroup_withGroupAdminBasicAuth_shouldReturnNoContent() throws Exception {
        long id = 42L;
        given(groupRepository.existsById(id)).willReturn(true);

        mvc.perform(delete("/groups/{id}", id)
                .with(httpBasic("group_admin", "group_admin")))
        .andExpect(status().isNoContent());
    }

    @Test
    void deleteGroup_withGroupOrganizerBasicAuth_shouldReturnForbidden() throws Exception {
        long id = 42L;
        given(groupRepository.existsById(id)).willReturn(true);

        mvc.perform(delete("/groups/{id}", id)
                .with(httpBasic("group_organizer", "group_organizer")))
        .andExpect(status().isForbidden());
    }

    @Test
    void deleteGroup_withInvalidBasicAuth_shouldReturnUnauthorized() throws Exception {
        long id = 42L;
        // No need to stub existsById: unauthorized at HTTP layer.

        mvc.perform(delete("/groups/{id}", id)
                .with(httpBasic("wrong", "creds")))
        .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "GROUP_ADMIN")
    void putGroup_shouldReturnOkForGroupAdmin() throws Exception {
        long id = 42L;
        Group updated = new Group("Updated Name");
        Group saved = new Group("Updated Name");
        String body = objectMapper.writeValueAsString(updated);

        given(groupRepository.findById(id)).willReturn(Optional.of(new Group("Old Name")));
        given(groupRepository.save(any(Group.class))).willReturn(saved);

        mvc.perform(put("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GROUP_ORGANIZER")
    void putGroup_shouldReturnForbiddenForNonAdmin() throws Exception {
        long id = 42L;
        Group updated = new Group("Updated Name");
        String body = objectMapper.writeValueAsString(updated);

        given(groupRepository.findById(id)).willReturn(Optional.of(new Group("Old Name")));

        mvc.perform(put("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isForbidden());
    }

    @Test
    void putGroup_shouldReturnUnauthorizedForAnonymous() throws Exception {
        long id = 42L;
        Group updated = new Group("Updated Name");
        String body = objectMapper.writeValueAsString(updated);

        mvc.perform(put("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void putGroup_withGroupAdminBasicAuth_shouldReturnOk() throws Exception {
        long id = 42L;
        Group updated = new Group("Updated Name");
        Group saved = new Group("Updated Name");
        String body = objectMapper.writeValueAsString(updated);

        given(groupRepository.findById(id)).willReturn(Optional.of(new Group("Old Name")));
        given(groupRepository.save(any(Group.class))).willReturn(saved);

        mvc.perform(put("/groups/{id}", id)
                .with(httpBasic("group_admin", "group_admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isOk());
    }

    @Test
    void putGroup_withGroupOrganizerBasicAuth_shouldReturnForbidden() throws Exception {
        long id = 42L;
        Group updated = new Group("Updated Name");
        String body = objectMapper.writeValueAsString(updated);

        given(groupRepository.findById(id)).willReturn(Optional.of(new Group("Old Name")));

        mvc.perform(put("/groups/{id}", id)
                .with(httpBasic("group_organizer", "group_organizer"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isForbidden());
    }

    @Test
    void putGroup_withInvalidBasicAuth_shouldReturnUnauthorized() throws Exception {
        long id = 42L;
        Group updated = new Group("Updated Name");
        String body = objectMapper.writeValueAsString(updated);

        mvc.perform(put("/groups/{id}", id)
                .with(httpBasic("wrong", "creds"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isUnauthorized());
    }

}
