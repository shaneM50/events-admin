package org.socialrunners.eventsadmin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must be at most 500 characters")
    @Column(nullable = false, length = 500)
    private String description;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String city;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false)
    private boolean active = true;

    @Email
    @Size(max = 255)
    @Column(length = 255)
    private String email;

    @Size(max = 255)
    @Column(length = 255)
    private String contactHandle;

    protected Group() {
        // for JPA
    }

    public Group(String name) {
        this.name = name;
    }

    public Group(String name,
                 String description,
                 String city,
                 String country,
                 boolean active,
                 String email,
                 String contactHandle) {
        this.name = name;
        this.description = description;
        this.city = city;
        this.country = country;
        this.active = active;
        this.email = email;
        this.contactHandle = contactHandle;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactHandle() {
        return contactHandle;
    }

    public void setContactHandle(String contactHandle) {
        this.contactHandle = contactHandle;
    }
}
