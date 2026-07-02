package org.socialrunners.eventsadmin.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.socialrunners.eventsadmin.config.TestSecurityConfig;
import org.socialrunners.eventsadmin.model.Group;
import org.socialrunners.eventsadmin.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
@WebMvcTest(GroupController.class)
@Import(TestSecurityConfig.class)
class GroupControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    GroupRepository groupRepository;

    private Group fullGroup(String name) {
        return new Group(
            name,
            "Some description for " + name,
            "Seville",
            "Spain",
            true,
            "contact@example.com",
            "@contact_handle"
        );
    }

    private ResultActions doGet(String url) throws Exception {
        return doGet(url, null);
    }

    private ResultActions doGet(String url, Map<String, String> params) throws Exception {
        MockHttpServletRequestBuilder request = get(url);

        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                request = request.param(entry.getKey(), entry.getValue());
            }
        }

        return mvc.perform(request);
    }

    private PageRequest pageableFromParams(Map<String, String> params) {
        int page = Integer.parseInt(params.getOrDefault("page", "0"));
        int size = Integer.parseInt(params.getOrDefault("size", "10"));
        String sortBy = params.getOrDefault("sortBy", "name");
        String direction = params.getOrDefault("direction", "asc");

        Sort sort = "desc".equalsIgnoreCase(direction)
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        return PageRequest.of(page, size, sort);
    }

    @Test
    void shouldReturnOkWhenGroupFound() throws Exception {
        Group group = fullGroup("NYC Runners");
        given(groupRepository.findById(99L)).willReturn(Optional.of(group));

        doGet("/groups/99")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("NYC Runners"));
    }

    @Test
    void shouldReturnNotFoundWhenNoGroupFound() throws Exception {
        given(groupRepository.findById(1L)).willReturn(Optional.empty());

        doGet("/groups/1")
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateGroupAndReturnCreated() throws Exception {
        Group newGroup = fullGroup("NYC Runners");
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
        // Missing name but still set required other fields
        Group newGroup = new Group(
            "",
            "Description",
            "Seville",
            "Spain",
            true,
            "contact@example.com",
            "@handle"
        );
        String newGroupJson = objectMapper.writeValueAsString(newGroup);

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
        Map<String, String> params = Map.of(); // no params -> controller defaults
        PageRequest defaultPageable = pageableFromParams(params);

        Group g1 = fullGroup("Alpha Runners");
        Group g2 = fullGroup("Zeta Runners");

        PageImpl<Group> pageResult = new PageImpl<>(List.of(g1, g2), defaultPageable, 2);

        given(groupRepository.findAll(defaultPageable)).willReturn(pageResult);

        doGet("/groups")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Alpha Runners"))
            .andExpect(jsonPath("$.content[1].name").value("Zeta Runners"))
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldApplyAllNonDefaultRequestParamsToPageable() throws Exception {
        Map<String, String> params = Map.of(
            "page", "2",
            "size", "5",
            "sortBy", "name",
            "direction", "desc"
        );

        PageRequest pageable = pageableFromParams(params);

        Group g1 = fullGroup("Zeta Runners");
        Group g2 = fullGroup("Alpha Runners");
        PageImpl<Group> pageResult = new PageImpl<>(List.of(g1, g2), pageable, 12);

        given(groupRepository.findAll(pageable)).willReturn(pageResult);

        doGet("/groups", params)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.number").value(2))
            .andExpect(jsonPath("$.size").value(5))
            .andExpect(jsonPath("$.totalElements").value(12))
            .andExpect(jsonPath("$.content[0].name").value("Zeta Runners"))
            .andExpect(jsonPath("$.content[1].name").value("Alpha Runners"));
    }

    @Test
    void shouldReturnEmptyPageWhenNoGroups() throws Exception {
        Map<String, String> params = Map.of(
            "page", "0",
            "size", "10"
        );
        PageRequest pageable = pageableFromParams(params);
        PageImpl<Group> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        given(groupRepository.findAll(pageable)).willReturn(emptyPage);

        doGet("/groups", params)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldDeleteExistingGroupAndReturnNoContent() throws Exception {
        long id = 99L;

        given(groupRepository.existsById(id)).willReturn(true);

        mvc.perform(delete("/groups/{id}", id))
           .andExpect(status().isNoContent());

        then(groupRepository).should().deleteById(id);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingGroup() throws Exception {
        long id = 123L;

        given(groupRepository.existsById(id)).willReturn(false);

        mvc.perform(delete("/groups/{id}", id))
           .andExpect(status().isNotFound());

        then(groupRepository).should(never()).deleteById(id);
    }

    @Test
    void shouldUpdateExistingGroupAndReturnOk() throws Exception {
        long id = 99L;
        Group existing = fullGroup("Old Name");
        Group updated = fullGroup("New Name");

        String updateJson = objectMapper.writeValueAsString(updated);

        given(groupRepository.findById(id)).willReturn(Optional.of(existing));
        given(groupRepository.save(any(Group.class))).willReturn(updated);

        mvc.perform(put("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.name").value(updated.getName()));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingGroup() throws Exception {
        long id = 123L;
        Group updated = fullGroup("New Name");
        String updateJson = objectMapper.writeValueAsString(updated);

        given(groupRepository.findById(id)).willReturn(Optional.empty());

        mvc.perform(put("/groups/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
           .andExpect(status().isNotFound());
    }
}
