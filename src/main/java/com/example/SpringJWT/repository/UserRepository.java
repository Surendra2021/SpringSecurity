package com.example.SpringJWT.repository;

import com.example.SpringJWT.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserRepository — Spring Data JPA repository for the User entity.
 *
 * By extending JpaRepository<User, Long>:
 *   - User   → entity type
 *   - Long   → type of the primary key (@Id field)
 *
 * Spring Data auto-generates all standard CRUD methods:
 *   save(), findById(), findAll(), delete(), etc.
 *
 * We only need to declare one custom method here:
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * findByUsername(String username)
     *
     * Spring Data parses the method name and generates:
     *   SELECT * FROM _User WHERE username = ?
     *
     * Returns Optional<User> so we can call .orElseThrow()
     * instead of doing a null check.
     */
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);


}

