package com.example.SpringJWT.service;

import com.example.SpringJWT.config.AppProperties;
import com.example.SpringJWT.dto.request.RegisterRequest;
import com.example.SpringJWT.exception.InvalidPasswordException;
import com.example.SpringJWT.exception.UserAlreadyExistsException;
import com.example.SpringJWT.entity.Role;
import com.example.SpringJWT.repository.RoleRepository;
import com.example.SpringJWT.repository.UserRepository;
import com.example.SpringJWT.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class) — tells JUnit to use Mockito
// so @Mock and @InjectMocks annotations work
@ExtendWith(5.class)
class AuthServiceTest {

    // @Mock — creates a fake version of each dependency
    // no real DB, no real JWT, no real password encoding
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AppProperties appProperties;

    // @InjectMocks — creates a real AuthService
    // and injects all the @Mock objects above into it
    @InjectMocks
    private AuthService authService;

    // @BeforeEach — runs before every single test
    // here we set the password min length from AppProperties mock
    @BeforeEach
    void setUp() {
        when(appProperties.getPasswordMinLength()).thenReturn(6);
    }

    // ─────────────────────────────────────────────────
    // TEST 1 — successful registration
    // ─────────────────────────────────────────────────
    @Test
    void shouldRegisterSuccessfully() {

        // ARRANGE — set up the inputs and mock responses
        RegisterRequest request = new RegisterRequest();
        request.setUsername("suren");
        request.setPassword("password123");
        request.setEmail("suren@test.com");

        // fake: username does not exist in DB
        when(userRepository.existsByUsername("suren")).thenReturn(false);

        // fake: email does not exist in DB
        when(userRepository.existsByEmail("suren@test.com")).thenReturn(false);

        // fake: role not found in DB — will be created
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        // fake: save role returns a Role object
        when(roleRepository.save(any(Role.class))).thenReturn(new Role("USER"));

        // fake: password encoding returns a hashed string
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        // fake: JWT generation returns a token string
        when(jwtService.generateToken(any())).thenReturn("fake-jwt-token");

        // ACT — call the actual method
        String token = authService.register(request);

        // ASSERT — check the result is what we expect
        assertEquals("fake-jwt-token", token);
    }

    // ─────────────────────────────────────────────────
    // TEST 2 — username already exists
    // ─────────────────────────────────────────────────
    @Test
    void shouldThrowWhenUsernameExists() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("suren");
        request.setPassword("password123");
        request.setEmail("suren@test.com");

        // fake: username already exists in DB
        when(userRepository.existsByUsername("suren")).thenReturn(true);

        // ASSERT — expect this specific exception to be thrown
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
    }

    // ─────────────────────────────────────────────────
    // TEST 3 — email already exists
    // ─────────────────────────────────────────────────
    @Test
    void shouldThrowWhenEmailExists() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("suren");
        request.setPassword("password123");
        request.setEmail("suren@test.com");

        // fake: username does not exist
        when(userRepository.existsByUsername("suren")).thenReturn(false);

        // fake: email already exists in DB
        when(userRepository.existsByEmail("suren@test.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
    }

    // ─────────────────────────────────────────────────
    // TEST 4 — password too short
    // ─────────────────────────────────────────────────
    @Test
    void shouldThrowWhenPasswordTooShort() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("suren");
        request.setPassword("abc"); // only 3 characters — less than min 6
        request.setEmail("suren@test.com");

        assertThrows(InvalidPasswordException.class, () -> authService.register(request));
    }
}
