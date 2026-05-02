package com.example.SpringJWT.repository;

import com.example.SpringJWT.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // Find a role by name e.g. findByName("ADMIN")
    Optional<Role> findByName(String name);
}
