package com.example.SpringJWT.service;

import com.example.SpringJWT.config.AppProperties;
import com.example.SpringJWT.event.UserRegisteredEvent;
import com.example.SpringJWT.exception.InvalidPasswordException;
import com.example.SpringJWT.exception.UserAlreadyExistsException;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppProperties appProperties;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    // Spring's built-in event publisher — fires events to any listener
    private final ApplicationEventPublisher eventPublisher;

    public String authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    }

    @Transactional
    public String register(RegisterRequest request) {

        if (request.getPassword().length() < appProperties.getPasswordMinLength()) {
            throw new InvalidPasswordException("Password must be at least " + appProperties.getPasswordMinLength() + " characters");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        // If no roles sent, default to USER
        List<String> roleNames = new ArrayList<>();
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            roleNames.add("USER");
        } else {
            roleNames = request.getRoles();
        }

        // For each role name, find it in DB or create it if not found
        List<Role> roles = new ArrayList<>();
        for (String roleName : roleNames) {
            String upperName = roleName.toUpperCase();
            Role role = roleRepository.findByName(upperName).orElse(null);
            if (role == null) {
                // role doesn't exist in DB yet — create and save it
                role = new Role(upperName);
                roleRepository.save(role);
            }
            roles.add(role);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roles);

        userRepository.save(user);

        // Build Spring Security authorities from roles
        // e.g. "ADMIN" → "ROLE_ADMIN"
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );

        // fire event — NotificationService picks this up and sends welcome email
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getUsername()));

        return jwtService.generateToken(userDetails);
    }
}
