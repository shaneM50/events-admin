package org.socialrunners.eventsadmin.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(
    name = "roles",
    uniqueConstraints = @UniqueConstraint(name = "uk_role_name", columnNames = "name")
)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Authority name used by Spring Security, e.g. "GROUP_ADMIN".
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Human-friendly description, e.g. "Can create and delete groups".
     */
    @Column(length = 255)
    private String description;

    protected Role() {
    }

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // For use in Sets
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
