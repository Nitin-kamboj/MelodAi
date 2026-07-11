package com.example.MelodAI.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class jwtUtil {

    private final String security_key = "SuperSecretMelodAIMusicGenerationAppTokenSigningKey2026!!!";
    private final long EXPIRATION_TIME = 86400000;

    private SecretKey getSignedKey(){
        return Keys.hmacShaKeyFor(security_key.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username){
        return Jwts.builder().subject(username).issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)).signWith(getSignedKey()).compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username) && !getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignedKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
