package org.socialrunners.eventsadmin.controller;

import org.junit.jupiter.api.Test;
import org.socialrunners.eventsadmin.model.Group;
import org.socialrunners.eventsadmin.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

@WebMvcTest(GroupController.class)
class GroupControllerTest {

    @Autowired
    MockMvc mvc;

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
}

