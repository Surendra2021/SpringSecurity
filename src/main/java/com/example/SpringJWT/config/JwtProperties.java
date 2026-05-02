package com.example.SpringJWT.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Secret key used to SIGN tokens (HS256).
     */
    private String secret;

    /**
     * Token validity duration in milliseconds.
     */
    private long expiration;
}
