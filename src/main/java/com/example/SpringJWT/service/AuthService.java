package com.example.SpringJWT.service;

import com.example.SpringJWT.config.AppProperties;
import com.example.SpringJWT.dto.request.AuthRequest;
import com.example.SpringJWT.dto.request.RegisterRequest;
import com.example.SpringJWT.entity.Role;
import com.example.SpringJWT.entity.User;
import com.example.SpringJWT.repository.RoleRepository;
import com.example.SpringJWT.repository.UserRepository;
import com.example.SpringJWT.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppProperties appProperties;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // to look up or create roles
    private final PasswordEncoder passwordEncoder;

    public String authenticate(AuthRequest request) {
        /**
         * ── Step 1: Authenticate ───────────────────────────────────────
         * UsernamePasswordAuthenticationToken packages credentials
         * into a format the AuthenticationManager understands.
         */
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), // principal (who)
                        request.getPassword()  // credentials (proof)
                )
        );

        /**
         * ── Step 2: Get UserDetails from the result ────────────────────
         * authentication.getPrincipal() returns the authenticated principal.
         * We cast it to UserDetails to pass to JwtService.
         */
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        /**
         * ── Step 3: Generate JWT ───────────────────────────────────────
         * jwtService.generateToken() creates a signed JWT.
         */
        return jwtService.generateToken(userDetails);
    }

    public String register(RegisterRequest request) {

        if (request.getPassword().length() < appProperties.getPasswordMinLength()) {
            throw new RuntimeException("Password must be at least " + appProperties.getPasswordMinLength() + " characters");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("UserEmail already exists");
        }

        // If no roles sent in request, default to ["USER"]
        List<String> roleNames = (request.getRoles() == null || request.getRoles().isEmpty())
                ? List.of("USER")
                : request.getRoles();

        // For each role name, find it in DB or create it if it doesn't exist yet
        // e.g. "ADMIN" → looks for Role{name="ADMIN"} in role table, creates if missing
        List<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name.toUpperCase(Locale.ROOT))
                        .orElseGet(() -> roleRepository.save(new Role(name.toUpperCase(Locale.ROOT)))))
                .toList();

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roles); // assign the list of Role objects to the user

        userRepository.save(user); // also saves to user_roles join table automatically

        // Build Spring Security authorities from the roles list
        // e.g. Role{name="ADMIN"} → SimpleGrantedAuthority("ROLE_ADMIN")
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );

        return jwtService.generateToken(userDetails);
    }
}
