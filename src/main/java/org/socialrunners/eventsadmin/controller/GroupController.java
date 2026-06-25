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

    @PreAuthorize("hasRole('GROUP_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable long id) {
        if (!groupRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        groupRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('GROUP_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Group> updateGroup(@PathVariable long id,
                                             @Valid @RequestBody Group updatedGroup) {
        return groupRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedGroup.getName());
                    Group saved = groupRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
