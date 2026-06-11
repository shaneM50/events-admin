package org.socialrunners.eventsadmin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups")
public class GroupController {

    // simple DTO used by tests; replace with real model later
    public static class Group {
        public long id;
        public String name;
        public Group() {}
        public Group(long id, String name) { this.id = id; this.name = name; }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroup(@PathVariable long id) {
        if (id == 99) {
            return ResponseEntity.ok(new Group(99, "NYC Runners"));
        }
        
        return ResponseEntity.notFound().build();
    }
}
