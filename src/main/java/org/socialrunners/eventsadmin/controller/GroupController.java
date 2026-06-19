package org.socialrunners.eventsadmin.controller;

import java.net.URI;
import org.socialrunners.eventsadmin.model.Group;
import org.socialrunners.eventsadmin.repository.GroupRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;
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

    @GetMapping
    public ResponseEntity<Page<Group>> getGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Group> groupsPage = groupRepository.findAll(pageable);

        return ResponseEntity.ok(groupsPage);
    }

    @PreAuthorize("hasRole('GROUP_ADMIN')")
    @PostMapping
    public ResponseEntity<Group> createGroup(@Valid @RequestBody Group group) {
        Group saved = groupRepository.save(group);
        URI location = URI.create("/groups/" + saved.getId());

        return ResponseEntity
                .created(location) 
                .body(saved);    
    }

}
