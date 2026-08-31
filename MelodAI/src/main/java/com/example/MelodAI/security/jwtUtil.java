package com.example.MelodAI.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class jwtUtil {

    private final String security_key = "SuperSecretMelodAIMusicGenerationAppTokenSigningKey2026!!!";
    private long accessTokenExpirationMs = 900000;
    private long refreshTokenExpirationMs = 604800000;

    private SecretKey getSignedKey(){
        return Keys.hmacShaKeyFor(security_key.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String username){
        return Jwts.builder().subject(username).issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs)).signWith(getSignedKey()).compact();
    }

    public String generateRefreshToken(String username){
        return Jwts.builder().subject(username).issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs)).signWith(getSignedKey()).compact();
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
    public boolean validateToken(String token) {
        try {
            getClaims(token); // Throws exception if invalid or expired
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
