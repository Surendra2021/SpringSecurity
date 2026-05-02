package com.example.SpringJWT.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 *  JwtAuthenticationFilter — The JWT Gatekeeper
 *
 *  This filter runs ONCE on every incoming HTTP request.
 *  It intercepts the Authorization header, validates the JWT,
 *  and tells Spring Security who the user is — before the request
 *  reaches any controller.
 *
 *  Flow:
 *    Request → [JwtAuthenticationFilter] → SecurityContext → Controller
 *
 *  Extends OncePerRequestFilter:
 *    Guarantees this filter executes exactly once per request,
 *    even in async dispatch scenarios.
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * @Component → registers as a Spring bean so Security can wire it in
 * @RequiredArgsConstructor → Lombok: inject final fields via constructor
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Service that generates and validates JWTs
    private final JwtService jwtService;

    // Service that loads user data from the database by username
    private final UserDetailsService userDetailsService;

    /**
     * Core filter logic. Called once per HTTP request.
     *
     * @param request     the incoming HTTP request
     * @param response    the outgoing HTTP response
     * @param filterChain the next filter in Spring Security's filter chain
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,   // @NonNull = null-safety hint
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ── STEP 1: Read the Authorization header ──────────────────────
        // Standard format: "Authorization: Bearer eyJhbGci..."
        final String authHeader = request.getHeader("Authorization");

        // If no Authorization header, or it doesn't start with "Bearer ",
        // skip JWT processing and pass the request to the next filter.
        // (The next filter or endpoint will handle unauthenticated access.)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // pass through
            return; // stop processing in this filter
        }

        // ── STEP 2: Extract the JWT from the header ────────────────────
        // "Bearer " is 7 characters → substring(7) strips the prefix
        // Result: the raw JWT token string "eyJhbGci..."
        final String jwt = authHeader.substring(7);

        // ── STEP 3: Extract the username from the JWT ──────────────────
        // jwtService.extractUsername() parses the "sub" (subject) claim.
        // If the token is malformed, this throws an exception → 403.
        final String username = jwtService.extractUsername(jwt);

        // ── STEP 4: Check if we should authenticate ────────────────────
        // username != null → token parsed successfully
        // getAuthentication() == null → user is NOT yet authenticated this request
        // (Avoid re-authenticating on every hop in a chain)
        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // ── STEP 5: Load user from DB ───────────────────────────────
            // UserDetailsService hits the database to get the full user record.
            // This is how we verify the username in the token is still valid.
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // ── STEP 6: Validate the token ──────────────────────────────
            // isTokenValid checks:
            //   a) username in token == username from DB
            //   b) token has not expired
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // ── STEP 7: Build the Authentication object ─────────────
                // UsernamePasswordAuthenticationToken is Spring Security's
                // standard authentication holder.
                // Constructor: (principal, credentials, authorities)
                //   - principal   = the UserDetails object (who the user is)
                //   - credentials = null (we don't need the password anymore)
                //   - authorities = list of roles/permissions
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,    // principal
                                null,           // no credentials needed post-authentication
                                userDetails.getAuthorities() // roles
                        );

                // ── STEP 8: Attach request details to the token ─────────
                // setDetails() enriches the auth object with extra metadata:
                //   IP address, session ID, etc. Used by audit logs/security events.
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // ── STEP 9: Store authentication in SecurityContext ──────
                // SecurityContextHolder is a thread-local store.
                // Putting the auth here tells all of Spring Security:
                //   "This request belongs to [username], roles=[...]"
                // From this point on, @PreAuthorize, hasRole(), and
                // Authentication injection in controllers will all work.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // ── STEP 10: Pass the request to the next filter ───────────────
        // Must always call this — otherwise the request chain stops here
        // and no controller ever gets the request.
        filterChain.doFilter(request, response);
    }
}

