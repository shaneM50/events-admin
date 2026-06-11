package org.socialrunners.eventsadmin.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GroupController.class)
class GroupControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void shouldReturnCashcard99() throws Exception {
        mvc.perform(get("/groups/99"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(99))
            .andExpect(jsonPath("$.name").value("NYC Runners"));
    }


    @Test
    void shouldReturnNotFoundForOtherId() throws Exception {
        mvc.perform(get("/groups/1"))
           .andExpect(status().isNotFound());
    }
}

