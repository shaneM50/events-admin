package org.socialrunners.eventsadmin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
}
