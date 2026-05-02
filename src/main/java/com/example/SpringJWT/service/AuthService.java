package com.example.SpringJWT.service;

import com.example.SpringJWT.config.AppProperties;
import com.example.SpringJWT.dto.request.AuthRequest;
import com.example.SpringJWT.dto.request.RegisterRequest;
import com.example.SpringJWT.entity.User;
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
    private final PasswordEncoder passwordEncoder;

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

        String role = (request.getRole() == null || request.getRole().isBlank())
                ? "USER"
                : request.getRole().trim().toUpperCase(Locale.ROOT);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );

        return jwtService.generateToken(userDetails);
    }
}
