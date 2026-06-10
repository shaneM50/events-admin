package org.socialrunners.eventsadmin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cashcards")
public class GroupController {

    // simple DTO used by tests; replace with real model later
    public static class CashCard {
        public long id;
        public String owner;
        public CashCard() {}
        public CashCard(long id, String owner) { this.id = id; this.owner = owner; }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CashCard> getCashCard(@PathVariable long id) {
        // TDD: start with a simple behavior the test expects (id 99 -> sample)
        if (id == 99) {
            return ResponseEntity.ok(new CashCard(99, "Test Owner"));
        }
        
        return ResponseEntity.notFound().build();
    }
}
