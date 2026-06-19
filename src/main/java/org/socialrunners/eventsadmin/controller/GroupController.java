package org.socialrunners.eventsadmin.controller;

import java.net.URI;

import org.socialrunners.eventsadmin.model.Group;
import org.socialrunners.eventsadmin.repository.GroupRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/groups")
public class GroupController {
    
    private final GroupRepository groupRepository;

    public GroupController(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroup(@PathVariable long id) {
        return groupRepository.findById(id)
            .map(ResponseEntity::ok)               
            .orElseGet(() -> ResponseEntity.notFound().build()); 
    }

    @PostMapping
    public ResponseEntity<Group> createGroup(@Valid @RequestBody Group group) {
        Group saved = groupRepository.save(group);
        URI location = URI.create("/groups/" + saved.getId());

        return ResponseEntity
                .created(location) 
                .body(saved);    
    }

}
