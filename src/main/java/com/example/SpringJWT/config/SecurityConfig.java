package com.example.SpringJWT.config;

import com.example.SpringJWT.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 *  SecurityConfig — The Security Blueprint
 *
 *  This class is the central hub of Spring Security.
 *  It wires together:
 *   - Which URLs are public vs. protected
 *   - Session management (stateless for JWT)
 *   - Password encoding
 *   - The authentication provider (DB lookup + password check)
 *   - Where our JWT filter plugs into the chain
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * @Configuration → tells Spring this class provides @Bean definitions
 * @EnableWebSecurity → activates Spring Security's web security support
 * @EnableMethodSecurity → enables @PreAuthorize on controller methods
 * @RequiredArgsConstructor → Lombok: constructor injection for final fields
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Our custom JWT filter (runs before the standard auth filter)
    private final JwtAuthenticationFilter jwtAuthFilter;

    // Our custom UserDetailsService (loads users from DB)
    private final UserDetailsService userDetailsService;

    /**
     * ── SecurityFilterChain ────────────────────────────────────────
     * This bean defines the ENTIRE HTTP security configuration.
     * It replaces the old WebSecurityConfigurerAdapter pattern.
     *
     * @param http  Spring's HttpSecurity builder — fluent DSL for security rules
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── 1. Disable CSRF ────────────────────────────────────────
            // CSRF (Cross-Site Request Forgery) protection uses cookies/sessions.
            // JWT is stateless (no sessions), so CSRF is irrelevant here.
            // Keeping it enabled would break REST API calls from mobile/SPA clients.
            .csrf(AbstractHttpConfigurer::disable)

            // ── 2. Authorization rules ─────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // permitAll() → no JWT required for these URLs
                // /api/auth/** covers /api/auth/login and /api/auth/register
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()

                // anyRequest().authenticated() → EVERY other URL needs a valid JWT.
                // If no valid token → Spring returns 403 Forbidden.
                .anyRequest().authenticated()
            )

            // ── 3. Session management ──────────────────────────────────
            // STATELESS = Spring will NEVER create an HttpSession.
            // Each request must carry its own JWT — there is no server-side session.
            // This is the KEY difference between JWT and traditional session-based auth.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ── 4. Authentication provider ─────────────────────────────
            // Tells Spring Security HOW to authenticate:
            //   "Look up the user in the DB via UserDetailsService,
            //    then compare the password using BCrypt"
            .authenticationProvider(authenticationProvider())

            // ── 5. Insert our JWT filter ───────────────────────────────
            // addFilterBefore() → run JwtAuthenticationFilter BEFORE
            // UsernamePasswordAuthenticationFilter in the filter chain.
            //
            // Why before? Our filter sets the SecurityContext from the JWT.
            // If Spring's built-in filter runs first, it would reject the
            // request before our JWT filter has a chance to authenticate it.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * ── AuthenticationProvider ─────────────────────────────────────
     * DaoAuthenticationProvider = "look up user in a DAO (database),
     * then verify the password".
     *
     * It connects:
     *   - UserDetailsService → "how to load the user"
     *   - PasswordEncoder    → "how to verify the password"
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

        // Tell it to use BCrypt for password comparison
        // (login password is BCrypt-hashed before comparing with DB hash)
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    /**
     * ── AuthenticationManager ──────────────────────────────────────
     * The AuthenticationManager is the entry point for all authentication.
     * We expose it as a @Bean so our AuthController can call it directly
     * when processing a login request.
     *
     * AuthenticationConfiguration auto-discovers the providers we configured.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * ── PasswordEncoder ────────────────────────────────────────────
     * BCryptPasswordEncoder hashes passwords with a random salt.
     * Features:
     *   - One-way: you can't reverse it to the original password
     *   - Slow by design: brute-force attacks are impractical
     *   - Salt included: same password → different hash every time
     *
     * Use this wherever you save passwords (e.g. register endpoint).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
