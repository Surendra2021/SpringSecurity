package com.example.SpringJWT.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
@Table(name = "_user")
public class User {

    @Id
    @GeneratedValue
    private Long id;

    private String username;
    private String password;
    private String email;

    // @ManyToMany — one user can have many roles, one role can belong to many users
    // @JoinTable — creates the "user_roles" join table in the DB
    //   joinColumns        → the column pointing to THIS table (_user)
    //   inverseJoinColumns → the column pointing to the OTHER table (role)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;
}


