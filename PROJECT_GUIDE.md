# 🔐 Complete JWT Spring Boot Project Guide (Simple English)

---

## 📚 What is this project?

A **user login system** that:
- Lets users **register** (create account)
- Lets users **login** (verify password)
- Gives users a **token** (digital ID card)
- Uses token to **access protected APIs** (like a security pass)

---

## 🏗️ Overall Architecture

```
User sends request
     ↓
JwtAuthenticationFilter (reads token from header)
     ↓
JwtService (validates token)
     ↓
UserDetailsServiceImpl (loads user from DB)
     ↓
Controller (does the work)
     ↓
Response back to user
```

---

# 📁 Project Structure & Files

## 1️⃣ `src/main/java/com/example/SpringJWT/DemoApplication.java`

**What is it?** The starting point of the whole application.

**Method:** `main(String[] args)`
- Simply starts Spring Boot application.
- Like pressing "play" button on your app.

**Built-in used:** `@SpringBootApplication` (Spring annotation)

---

## 2️⃣ `src/main/java/com/example/SpringJWT/User/User.java`

**What is it?** The database table structure (what fields a user has).

**Fields:**
- `id` - unique number (1, 2, 3...)
- `username` - your name (like "suren1")
- `password` - secret (hashed/encrypted)
- `email` - email address
- `role` - user type ("USER" or "ADMIN")

**Annotations used:**
- `@Entity` - tells Spring this is a database table
- `@Table(name = "_User")` - table name in DB is "_User"
- `@Id` - this field is the unique key
- `@GeneratedValue` - auto-creates ID (1, 2, 3...)

**Built-in used:** JPA (Java Persistence API) - automatically saves to database

---

## 3️⃣ `src/main/java/com/example/SpringJWT/User/UserRepository.java`

**What is it?** The database helper (talks to the User table).

**Methods:**

### `findByUsername(String username)`
- Finds ONE user by their username.
- Returns `Optional<User>` (might be empty if not found).
- Used during **login**.

### `existsByUsername(String username)`
- Checks if username already exists (true/false).
- Used during **register** to stop duplicates.

### `existsByEmail(String email)`
- Checks if email already exists (true/false).
- Used during **register** to stop duplicate emails.

**Built-in used:** `JpaRepository` (Spring Data - auto-creates SQL queries)

---

## 4️⃣ `src/main/java/com/example/SpringJWT/auth/AuthRequest.java`

**What is it?** Holds login data that client sends.

**Fields:**
- `username` - user typed this
- `password` - user typed this

**Built-in used:** `@Data` (Lombok - auto-creates getters/setters)

---

## 5️⃣ `src/main/java/com/example/SpringJWT/auth/RegisterRequest.java`

**What is it?** Holds registration data that client sends.

**Fields:**
- `username` - user wants this name
- `password` - user wants this password
- `email` - user's email
- `role` - user wants this role (or blank = defaults to "USER")

**Built-in used:** `@Data` (Lombok)

---

## 6️⃣ `src/main/java/com/example/SpringJWT/auth/AuthController.java`

**What is it?** The **login/register door** - where users start.

### Method 1: `login(AuthRequest request)`

**What it does:**
1. Client sends username + password
2. Check database: does user exist? is password correct?
3. If YES → create JWT token → return token
4. If NO → return 401 (unauthorized)

**Flow:**
```
Input: { username: "suren1", password: "pass123" }
  ↓
authenticationManager.authenticate() 
  → loads user from DB
  → compares password with BCrypt
  ↓
IF password matches:
  ↓
jwtService.generateToken(userDetails)
  → creates token
  ↓
Output: JWT token (like eyJhbGci...)
```

**Built-in used:**
- `AuthenticationManager` (Spring Security - verifies credentials)
- `UsernamePasswordAuthenticationToken` (Spring Security - holds credentials)
- `PasswordEncoder` (BCrypt - password verification)

---

### Method 2: `register(RegisterRequest request)`

**What it does:**
1. Client sends username + password + email + role
2. Check: username already exists? email already exists?
3. If YES → return 409 (conflict)
4. If NO:
   - Hash password with BCrypt
   - Set role to "USER" if blank
   - Save user to database
   - Create JWT token
   - Return token

**Flow:**
```
Input: { username: "suren2", password: "pass123", email: "suren2@mail.com" }
  ↓
Check if username exists → if yes, return 409
Check if email exists → if yes, return 409
  ↓
Hash password: "pass123" → "$2a$10$..." (one-way encryption)
Set role: "USER" (default)
  ↓
Save to database
  ↓
Create JWT token
  ↓
Output: JWT token (like eyJhbGci...)
```

**Built-in used:**
- `PasswordEncoder.encode()` (BCrypt - hashes password)
- `UserRepository.save()` (JPA - saves to database)

---

## 7️⃣ `src/main/java/com/example/SpringJWT/security/JwtService.java`

**What is it?** The token expert (creates and validates JWT tokens).

### Method 1: `generateToken(UserDetails userDetails)`

**What it does:** Creates a JWT token for a user.

**Flow:**
```
Input: UserDetails (username + roles)
  ↓
Get current time
Get expiration time (current + 24 hours)
  ↓
Create JWT with:
  - subject (sub): username
  - issued at (iat): now
  - expiration (exp): now + 24 hours
  ↓
Sign with secret key using HMAC-SHA256
  (signature proves we created it, not someone else)
  ↓
Output: JWT string (3 parts separated by dots)
  Part 1: header (encoding info)
  Part 2: payload (username + dates)
  Part 3: signature (proof)
```

**Built-in used:** `Jwts.builder()` (JJWT library - builds JWT)

---

### Method 2: `extractUsername(String token)`

**What it does:** Reads username from a JWT token.

**Flow:**
```
Input: JWT token (eyJhbGci...)
  ↓
Parse and verify signature
  ↓
Get the "sub" (subject) claim
  ↓
Output: username (like "suren1")
```

**Built-in used:** `Claims::getSubject` (Java method reference)

---

### Method 3: `isTokenValid(String token, UserDetails userDetails)`

**What it does:** Checks if token is real and not expired.

**Checks:**
1. Username in token == username in database?
2. Token expiration time > current time?

**Output:** true (valid) or false (invalid)

**Built-in used:** `Date.before()` (Java time comparison)

---

### Method 4: `extractAllClaims(String token)` (private)

**What it does:** Parses JWT and verifies signature.

**Important:** If signature is wrong (token was changed), it throws exception.

**Built-in used:** `Jwts.parser().verifyWith().parseSignedClaims()` (JJWT)

---

### Method 5: `getSigningKey()` (private)

**What it does:** Converts secret key to format needed for signing.

**Flow:**
```
Secret in YAML: "MySuperSecretKeyForJWTThatIsAtLeast256BitsLong!!"
  ↓
Decode from Base64
  ↓
Create HMAC-SHA256 key
  ↓
Use for signing/verifying
```

**Built-in used:** `Decoders.BASE64URL.decode()`, `Keys.hmacShaKeyFor()` (JJWT)

---

## 8️⃣ `src/main/java/com/example/SpringJWT/security/UserDetailsServiceImpl.java`

**What is it?** Loads user from database during login/token validation.

### Method: `loadUserByUsername(String username)`

**What it does:**
1. Get username (from login form or JWT token)
2. Search database for that user
3. If found:
   - Convert to Spring Security's UserDetails format
   - Add role as "ROLE_" + user's role
   - Return it
4. If not found:
   - Throw exception (causes 401 error)

**Flow:**
```
Input: username = "suren1"
  ↓
userRepository.findByUsername("suren1")
  ↓
IF found:
  Create UserDetails with:
    - username: "suren1"
    - password: stored hash
    - authorities: ["ROLE_USER"]
  ↓
  Output: UserDetails
  ↓
IF not found:
  Throw UsernameNotFoundException
  ↓
Output: 401 Unauthorized
```

**Built-in used:**
- `UserDetailsService` (Spring Security interface)
- `Optional.orElseThrow()` (Java)
- `SimpleGrantedAuthority` (Spring Security)

---

## 9️⃣ `src/main/java/com/example/SpringJWT/security/JwtAuthenticationFilter.java`

**What is it?** The security checkpoint - runs on EVERY request to protected endpoints.

### Method: `doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)`

**What it does:** Intercepts request, validates JWT, sets user in SecurityContext.

**Step-by-step:**
```
Step 1: Read Authorization header
  Example: "Bearer eyJhbGci..."
  ↓
Step 2: Check if header exists and starts with "Bearer "
  If NO → skip JWT check, pass request to next filter
  If YES → extract token
  ↓
Step 3: Extract username from JWT
  jwtService.extractUsername(token)
  ↓
Step 4: Load user from database
  userDetailsService.loadUserByUsername(username)
  ↓
Step 5: Validate token
  jwtService.isTokenValid(token, userDetails)
  ↓
IF valid:
  Create authentication object
  Store in SecurityContext (thread-local storage)
  Now Spring knows this request is from "suren1" with role "USER"
  ↓
IF invalid:
  Do nothing, request continues (controller will see no user)
  ↓
Step 6: Pass to next filter
  filterChain.doFilter(request, response)
```

**Built-in used:**
- `OncePerRequestFilter` (Spring - runs once per request)
- `SecurityContextHolder` (Spring - thread-local user storage)
- `UsernamePasswordAuthenticationToken` (Spring Security)

---

## 🔟 `src/main/java/com/example/SpringJWT/config/SecurityConfig.java`

**What is it?** The security blueprint - tells Spring which URLs are public, which need tokens, etc.

### Bean 1: `securityFilterChain(HttpSecurity http)`

**What it does:** Configures all security rules.

**Rules:**
```
1. CSRF disabled
   (CSRF is for websites with cookies, we use stateless JWT)
   ↓
2. Public URLs: /api/auth/** (register, login don't need token)
   Protected URLs: everything else (need valid JWT)
   ↓
3. Session = STATELESS
   (no server-side session, each request carries JWT)
   ↓
4. Add JWT filter BEFORE built-in auth filter
   (so our filter runs first)
```

**Built-in used:** `HttpSecurity` (Spring fluent API)

---

### Bean 2: `authenticationProvider()`

**What it does:** Tells Spring HOW to authenticate (how to check password).

**How:**
```
Use UserDetailsServiceImpl to load user
Use PasswordEncoder (BCrypt) to verify password
```

**Built-in used:** `DaoAuthenticationProvider` (Spring Security)

---

### Bean 3: `authenticationManager(AuthenticationConfiguration config)`

**What it does:** The main entry point for authentication.

Used in: `AuthController.login()` to verify credentials.

**Built-in used:** `AuthenticationConfiguration` (Spring)

---

### Bean 4: `passwordEncoder()`

**What it does:** Creates BCrypt password encoder (one-way encryption).

**How it works:**
```
User password: "pass123"
  ↓
BCrypt.encode("pass123")
  ↓
Output: "$2a$10$salt...hash..." (different every time)
  ↓
When user logins:
  BCrypt.matches("pass123", "$2a$10$salt...hash...")
  ↓
Output: true (matches!)
```

**Why BCrypt?**
- One-way (can't decode back)
- Slow (brute-force attacks take forever)
- Includes salt (same password = different hash)

**Built-in used:** `BCryptPasswordEncoder` (Spring Security)

---

## 1️⃣1️⃣ `src/main/java/com/example/SpringJWT/controller/HelloController.java`

**What is it?** Sample protected endpoints (needs valid JWT to access).

### Method 1: `hello(Authentication authentication)`

**What it does:** Simple endpoint that shows you're logged in.

**Example:**
```
GET /api/hello
Authorization: Bearer <token>
  ↓
Response: "Hello, suren1! Your JWT is valid."
```

**Built-in used:** Spring auto-injects `Authentication` from SecurityContext.

---

### Method 2: `adminOnly()`

**What it does:** Endpoint ONLY for ADMIN role.

**Security:**
- `@PreAuthorize("hasRole('ADMIN')")` checks role BEFORE method runs
- If user is not ADMIN → 403 Forbidden
- If user is ADMIN → method runs

**Example:**
```
User role: USER → 403 Forbidden
User role: ADMIN → "Welcome, Admin! This is a restricted area."
```

**Built-in used:** `@PreAuthorize` (Spring Security)

---

## 1️⃣2️⃣ `src/main/resources/application.yml`

**What is it?** Configuration file (settings for the app).

### Database section:
```yaml
datasource:
  url: jdbc:postgresql://localhost:5432/
  username: postgres
  password: welcome
```
- Tells app where database is and how to login

### JWT section:
```yaml
jwt:
  secret: "MySuperSecretKeyForJWTThatIsAtLeast256BitsLong!!"
  expiration: 86400000  (24 hours in milliseconds)
```
- Secret key used to sign tokens
- How long token is valid

---

# 🔄 Complete Request Flow Examples

## Example 1: Register New User

```
1. Client sends:
   POST /api/auth/register
   {
     "username": "suren1",
     "password": "pass123",
     "email": "suren1@mail.com",
     "role": "USER"
   }

2. AuthController.register() receives it
   ↓
3. Check: username exists? No ✓
4. Check: email exists? No ✓
   ↓
5. Hash password: "pass123" → "$2a$10$..."
6. Save to database
   ↓
7. JwtService.generateToken() creates JWT
   ↓
8. Return JWT token

Response: 201 Created
Body: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdXJlbjEifQ...
```

---

## Example 2: Login

```
1. Client sends:
   POST /api/auth/login
   {
     "username": "suren1",
     "password": "pass123"
   }

2. AuthController.login() receives it
   ↓
3. AuthenticationManager.authenticate()
   → UserDetailsServiceImpl loads user from DB
   → PasswordEncoder.matches() checks password
   ✓ Password matches!
   ↓
4. Get UserDetails
   ↓
5. JwtService.generateToken() creates JWT
   ↓
6. Return JWT token

Response: 200 OK
Body: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdXJlbjEifQ...
```

---

## Example 3: Access Protected Endpoint

```
1. Client sends:
   GET /api/hello
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

2. JwtAuthenticationFilter.doFilterInternal() intercepts
   ↓
3. Read Authorization header
   ↓
4. Extract token
   ↓
5. JwtService.extractUsername() gets "suren1" from token
   ↓
6. UserDetailsServiceImpl.loadUserByUsername() loads user from DB
   ↓
7. JwtService.isTokenValid() checks:
   - Username matches? ✓
   - Not expired? ✓
   ↓
8. Create authentication object
   ↓
9. Set in SecurityContext
   ↓
10. Request goes to HelloController.hello()
   ↓
11. Method runs using Authentication object

Response: 200 OK
Body: "Hello, suren1! Your JWT is valid."
```

---

## Example 4: Invalid Token

```
1. Client sends:
   GET /api/hello
   Authorization: Bearer INVALIDTOKEN

2. JwtAuthenticationFilter extracts token
   ↓
3. JwtService.extractUsername() tries to parse
   ↓
4. Signature verification FAILS
   → Exception thrown
   ↓
5. SecurityContext has NO user
   ↓
6. Request reaches controller with no authentication
   ↓
7. Spring Security returns 403 Forbidden

Response: 403 Forbidden
```

---

# 📚 Built-in Tools Summary

| Tool | What it does | Where used |
|------|-------------|-----------|
| **Spring Security** | User authentication & authorization | Whole project |
| **Spring Data JPA** | Database ORM (auto-generates SQL) | UserRepository |
| **BCrypt** | Password hashing (one-way encryption) | SecurityConfig, AuthController |
| **JJWT** | Create & validate JWT tokens | JwtService |
| **Lombok** | Auto-generates getters/setters | User, AuthRequest, RegisterRequest |
| **Hibernate** | Database mapping | User entity |
| **PostgreSQL** | Database (stores users) | application.yml |

---

# 🎯 Key Concepts

### JWT Token (3 parts)
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9 . eyJzdWIiOiJzdXJlbjEiLCJpYXQiOjE3MTI2MTEyMDB9 . signature
       ↑ header                                    ↑ payload (username, dates)           ↑ proof
```

### SecurityContext
- Thread-local storage (one per request)
- Holds who the current user is
- Accessible everywhere in request

### Stateless = no session
- Each request is independent
- No server memory needed
- JWT carries all info needed

### BCrypt
- Password is hashed (encrypted one-way)
- Can't get original password back
- Slow on purpose (stops hackers)

---

# ✅ Summary

**The flow:**
1. Register → hash password → save user → return JWT
2. Login → check password → return JWT
3. Protected API → read JWT → validate → set user → allow access

**The secret:** JWT is like a digital ID card signed by server. No one can fake it because only server knows the secret key.


