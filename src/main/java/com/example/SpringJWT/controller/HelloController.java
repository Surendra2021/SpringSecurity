package com.example.SpringJWT.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 *  HelloController — Protected Endpoint Demo
 *
 *  These endpoints require a valid JWT.
 *  If the client doesn't send "Authorization: Bearer <token>",
 *  Spring Security returns 403 Forbidden before this code runs.
 * ╚══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    /**
     * GET /api/hello
     *
     * Any authenticated user can access this (any valid JWT).
     *
     * @param authentication  Spring auto-injects this from the SecurityContext.
     *                        It contains the UserDetails set by JwtAuthenticationFilter.
     * @return                greeting with the logged-in username
     */
    @GetMapping("/hello")
    public ResponseEntity<String> hello(Authentication authentication) {
        // authentication.getName() → returns the username from the JWT subject claim
        return ResponseEntity.ok("Hello, " + authentication.getName() + "! Your JWT is valid.");
    }

    /**
     * GET /api/admin
     *
     * @PreAuthorize("hasRole('ADMIN')") → Spring checks the user's granted authorities
     * BEFORE executing this method. If the user does not have ROLE_ADMIN,
     * Spring returns 403 Forbidden immediately.
     *
     * This requires @EnableMethodSecurity in SecurityConfig.
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Welcome, Admin! This is a restricted area.");
    }
}
