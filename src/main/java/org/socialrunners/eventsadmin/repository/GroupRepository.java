package org.socialrunners.eventsadmin.repository;

import org.socialrunners.eventsadmin.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}