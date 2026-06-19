package org.socialrunners.eventsadmin.controller;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.socialrunners.eventsadmin.config.TestSecurityConfig;
import org.socialrunners.eventsadmin.model.Group;
import org.socialrunners.eventsadmin.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

@WebMvcTest(GroupController.class)
@Import(TestSecurityConfig.class)
/**
 * Controller slice tests for GroupController.
 *
 * These tests focus on controller behavior (status codes, JSON, paging, validation)
 * and deliberately ignore security concerns. TestSecurityConfig overrides the real
 * SecurityConfig to permit all requests at the HTTP layer, so we don't need an
 * authenticated user or IdP setup in this test slice.
 *
 * Security rules (roles, forbidden/allowed) are covered separately.
 */
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

    @Test
    void shouldUseDefaultPagingAndSortingWhenNoParams() throws Exception {
        PageRequest defaultPageable = PageRequest.of(
            0, 
            10, 
            Sort.by("name")
            .ascending());

        Group g1 = new Group("Alpha Runners");
        Group g2 = new Group("Zeta Runners");

        PageImpl<Group> pageResult = new PageImpl<>(List.of(g1, g2), defaultPageable, 2);
        
        given(groupRepository.findAll(defaultPageable)).willReturn(pageResult);

        mvc.perform(get("/groups"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Alpha Runners"))
            .andExpect(jsonPath("$.content[1].name").value("Zeta Runners"))
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldApplyAllNonDefaultRequestParamsToPageable() throws Exception {
        int page = 2;          
        int size = 5;          
        String sortBy = "name";
        String direction = "desc";

        PageRequest pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        Group g1 = new Group("Zeta Runners");
        Group g2 = new Group("Alpha Runners");
        PageImpl<Group> pageResult = new PageImpl<>(List.of(g1, g2), pageable, 12);

        given(groupRepository.findAll(pageable)).willReturn(pageResult);

        mvc.perform(get("/groups")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .param("sortBy", sortBy)
                .param("direction", direction))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.number").value(page))
            .andExpect(jsonPath("$.size").value(size))
            .andExpect(jsonPath("$.totalElements").value(12))
            .andExpect(jsonPath("$.content[0].name").value("Zeta Runners"))
            .andExpect(jsonPath("$.content[1].name").value("Alpha Runners"));
    }

    @Test
    void shouldReturnEmptyPageWhenNoGroups() throws Exception {
        int page = 0;
        int size = 10;
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        PageImpl<Group> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        given(groupRepository.findAll(pageable)).willReturn(emptyPage);

        mvc.perform(get("/groups")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.totalElements").value(0));
    }

}

