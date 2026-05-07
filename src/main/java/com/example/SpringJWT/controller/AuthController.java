package com.example.SpringJWT.controller;

import com.example.SpringJWT.dto.response.UserResponse;
import com.example.SpringJWT.exception.UserNotFoundException;
import com.example.SpringJWT.service.AuthService;
import com.example.SpringJWT.dto.request.AuthRequest;
import com.example.SpringJWT.dto.request.RegisterRequest;
import com.example.SpringJWT.dto.response.AuthResponse;
import com.example.SpringJWT.entity.User;
import com.example.SpringJWT.repository.UserRepository;
import com.example.SpringJWT.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 *  AuthController — The Login Door
 *
 *  Exposes a single public endpoint: POST /api/auth/login
 *
 *  Flow:
 *   1. Client sends { "username": "suren", "password": "secret" }
 *   2. We ask Spring Security to authenticate (check DB + BCrypt)
 *   3. On success → generate JWT and return it
 *   4. On failure → Spring throws BadCredentialsException → 401
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * @RestController = @Controller + @ResponseBody
 *   Every method return value is written directly to HTTP response body as JSON
 * @RequestMapping("/api/auth") → base path for all endpoints here
 * @RequiredArgsConstructor → Lombok constructor injection
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Service for handling authentication logic
    private final AuthService authService;

    // Entry point for all authentication — wired up in SecurityConfig
    //This manages authentication of user
    private final AuthenticationManager authenticationManager;

    // Our JWT utility — generates and validates tokens
    private final JwtService jwtService;

    // Repository used for creating new users and duplicate checks
    private final UserRepository userRepository;

    // BCrypt encoder bean configured in SecurityConfig
    private final PasswordEncoder passwordEncoder;

    /**
     * POST /api/auth/login
     *
     * Public endpoint (no JWT required — configured in SecurityConfig).
     *
     * @param request  JSON body { "username": "...", "password": "..." }
     *                 mapped to AuthRequest via @RequestBody
     * @return         200 OK + JWT token string on success
     *                 401 Unauthorized if credentials are wrong
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {

        /**
         * ── Step 4: Return token in response body ──────────────────────
         * ResponseEntity.ok(token) → HTTP 200 OK with the JWT string as body.
         *
         * The client should store this token and send it on future requests:
         *   Authorization: Bearer <token>
         */
        String jwtToken = authService.authenticate(request);
        return ResponseEntity.ok(AuthResponse.builder().token(jwtToken).build());
    }

    /**
     * POST /api/auth/register
     *
     * Public endpoint to create a new user.
     * - Rejects duplicate username/email with 409
     * - Hashes password using BCrypt
     * - Defaults role to USER when missing/blank
     * - Returns JWT for immediate authenticated use
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {


        String jwtToken = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder().token(jwtToken).build());
    }

    @GetMapping("/users")
    public UserResponse getUsers(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new UserNotFoundException("User not found"));
        UserResponse response = new UserResponse();
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        return response;
    }
}

