package com.example.SpringJWT.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

// This is a DB table called "role"
// Each row is one role e.g. ADMIN, USER, MODERATOR
@Entity
@Data
@NoArgsConstructor
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue
    private Long id;

    // The role name e.g. "ADMIN", "USER"
    private String name;

    // Convenience constructor — so you can do new Role("ADMIN")
    public Role(String name) {
        this.name = name;
    }
}
