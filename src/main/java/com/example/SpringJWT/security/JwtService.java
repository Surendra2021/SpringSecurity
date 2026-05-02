package com.example.SpringJWT.security;

import com.example.SpringJWT.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * ╔══════════════════════════════════════════════════════════╗
 *  JwtService — The JWT Brain
 *  Responsible for:
 *   1. Generating tokens
 *   2. Extracting claims (data) from tokens
 *   3. Validating tokens
 * ╚══════════════════════════════════════════════════════════╝
 *
 * @Service → Registers this class as a Spring-managed bean.
 *            It can be @Autowired / injected wherever needed.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    // ─────────────────────────────────────────────────────────
    // 1. TOKEN GENERATION
    // ─────────────────────────────────────────────────────────

    /**
     * Generates a token with ONLY the username as the subject.
     * No extra claims.
     *
     * @param userDetails  Spring Security object holding username + roles
     * @return             signed JWT string  e.g. "eyJhbGci..."
     */
    public String generateToken(UserDetails userDetails) {
        // Pass empty extra claims map → token will only have standard claims
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Core token builder. Called by the public generateToken above.
     *
     * @param extraClaims  any custom key-value pairs to embed (e.g. role, userId)
     * @param userDetails  Spring Security user object
     * @return             compact JWT string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                // setClaims() → embed any custom claims (must be called FIRST
                //                before subject/dates so they are not overwritten)
                .claims(extraClaims)

                // subject() → the "sub" claim: identifies WHO the token is for
                //              Usually the username or user ID
                .subject(userDetails.getUsername())

                // issuedAt() → the "iat" claim: when was this token created?
                //              Used to detect old tokens if you change the secret.
                .issuedAt(new Date(System.currentTimeMillis()))

                // expiration() → the "exp" claim: when does this token expire?
                //                After this time, the token is rejected automatically.
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))

                // signWith() → cryptographically signs the token header+payload
                //              with HMAC-SHA256. Without this, anyone could forge tokens.
                .signWith(getSigningKey())

                // compact() → serializes everything into the final 3-part JWT string:
                //              BASE64(header).BASE64(payload).BASE64(signature)
                .compact();
    }

    // ─────────────────────────────────────────────────────────
    // 2. CLAIM EXTRACTION
    // ─────────────────────────────────────────────────────────

    /**
     * Extracts the username ("sub" claim) from a token.
     * This is the most-used extraction — called on every request.
     *
     * @param token  raw JWT string from Authorization header
     * @return       the username stored inside the token
     */
    public String extractUsername(String token) {
        // Claims::getSubject → method reference that calls getSubject()
        //                      on the Claims object returned by extractAllClaims()
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic claim extractor. Uses a Function to pull ANY field
     * from the claims payload without duplicating parse logic.
     *
     * @param token          raw JWT string
     * @param claimsResolver a function like Claims::getSubject or
     *                       Claims::getExpiration
     * @param <T>            return type inferred from the function
     * @return               the extracted value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        // 1. Parse the full token and get all claims
        final Claims claims = extractAllClaims(token);
        // 2. Apply the caller-supplied function to pull the specific claim
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the JWT and returns ALL claims (the entire payload map).
     * This is where the signature is verified — if the token was
     * tampered with or signed with a different key, this throws
     * JwtException and the request is rejected.
     *
     * @param token  raw JWT string
     * @return       Claims object (a Map of all payload key-value pairs)
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                // verifyWith() → sets the key used to verify the signature.
                //                Must match the key used during signWith().
                .verifyWith(getSigningKey())
                .build()
                // parseSignedClaims() → verifies signature AND parses payload.
                //                      Throws SignatureException if tampered.
                .parseSignedClaims(token)
                // getPayload() → returns the Claims map from the verified token
                .getPayload();
    }

    // ─────────────────────────────────────────────────────────
    // 3. VALIDATION
    // ─────────────────────────────────────────────────────────

    /**
     * Full token validation:
     *   1. Username in token matches the UserDetails in memory/DB
     *   2. Token has not expired
     *
     * @param token        raw JWT string
     * @param userDetails  loaded from DB by UserDetailsService
     * @return             true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Two conditions MUST both be true:
        //   a) token subject matches the logged-in user's username
        //   b) token expiry time is in the future
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if the token's expiration date is before right now.
     *
     * @param token  raw JWT string
     * @return       true if expired, false if still valid
     */
    private boolean isTokenExpired(String token) {
        // extractExpiration() returns a Date object for the "exp" claim
        // .before(new Date()) → true if that date is in the past
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date ("exp" claim) from the token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ─────────────────────────────────────────────────────────
    // 4. KEY MANAGEMENT
    // ─────────────────────────────────────────────────────────

    /**
     * Converts the Base64-encoded secret string from application.yml
     * into a Java SecretKey object suitable for HMAC-SHA256 signing.
     *
     * Why Base64?
     *   Storing raw bytes in YAML is error-prone. Base64 is text-safe.
     *   Decoders.BASE64URL.decode() converts it back to the raw byte[].
     *
     * Keys.hmacShaKeyFor() ensures the key is the right length for HS256.
     */
    private SecretKey getSigningKey() {
        // Step 1: Decode the Base64 string → raw byte[]
        byte[] keyBytes = Decoders.BASE64URL.decode(jwtProperties.getSecret());
        // Step 2: Wrap byte[] in a SecretKey object for JJWT
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

