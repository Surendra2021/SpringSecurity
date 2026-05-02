package com.example.SpringJWT.service;

import com.example.SpringJWT.entity.User;
import com.example.SpringJWT.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════╗
 *  UserDetailsServiceImpl
 *
 *  Spring Security calls this class whenever it needs to load
 *  a user's data during authentication.
 *
 *  The ONE method you MUST implement: loadUserByUsername()
 *  It bridges YOUR User entity → Spring Security's UserDetails
 * ╚══════════════════════════════════════════════════════════╝
 *
 * @Service → registers as a Spring bean
 * @RequiredArgsConstructor (Lombok) → generates a constructor
 *   that injects all final fields (userRepository here)
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * Spring Data JPA repository for the User entity.
     * final → Lombok will inject it via constructor.
     */
    private final UserRepository userRepository;

    /**
     * Spring Security calls this with the username submitted at login.
     * Your job: load the user from the database and return a UserDetails.
     *
     * @param username  submitted by the client at login
     * @return          UserDetails object (Spring Security's user model)
     * @throws UsernameNotFoundException  if no user found — Spring turns
     *                                    this into a 401 Unauthorized response
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Query the DB for the user. orElseThrow() bubbles a
        // UsernameNotFoundException if the username doesn't exist.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + username));

        /**
         * SimpleGrantedAuthority wraps a role string (e.g. "ROLE_USER")
         * into the GrantedAuthority interface Spring Security uses for
         * access-control checks (@PreAuthorize, hasRole(), etc.)
         *
         * Prefix "ROLE_" is a Spring Security convention.
         * e.g., if user.getRole() = "USER" → authority = "ROLE_USER"
         */
        // Convert each Role entity → SimpleGrantedAuthority with "ROLE_" prefix
        // e.g. Role{name="ADMIN"} → SimpleGrantedAuthority("ROLE_ADMIN")
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();

        // Spring Security's built-in User — bridges YOUR User entity to Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}

