package org.socialrunners.eventsadmin.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.socialrunners.eventsadmin.model.Group;
import org.socialrunners.eventsadmin.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.Optional;

@WebMvcTest(GroupController.class)
class GroupControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    GroupRepository groupRepository;

    @Test
    void shouldReturnOkWhenGroupFound() throws Exception {
        Group group = new Group("NYC Runners");
        given(groupRepository.findById(99L)).willReturn(Optional.of(group));

        mvc.perform(get("/groups/99"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.name").value("NYC Runners"));
    }

    @Test
    void shouldReturnNotFoundWhenNoGroupFound() throws Exception {
        given(groupRepository.findById(1L)).willReturn(Optional.empty());

        mvc.perform(get("/groups/1"))
           .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateGroupAndReturnCreated() throws Exception {
        Group newGroup = new Group("NYC Runners"); 
        String newGroupJson = objectMapper.writeValueAsString(newGroup);

        given(groupRepository.save(any(Group.class))).willReturn(newGroup);

        mvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newGroupJson))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", Matchers.containsString("/groups/")))
            .andExpect(jsonPath("$.name").value("NYC Runners"));
    }

    @Test
    void shouldReturnBadRequestWhenNameMissing() throws Exception {
        Group newGroup = new Group(""); 
        String newGroupJson = objectMapper.writeValueAsString(newGroup);

        given(groupRepository.save(any(Group.class))).willReturn(newGroup);

        mvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newGroupJson))
            .andExpect(status().isBadRequest());    
    }

    @Test
    void shouldReturnBadRequestWhenJsonInvalid() throws Exception {
        String invalidJson = "{ name: NYC Runners "; 

        mvc.perform(post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

}

