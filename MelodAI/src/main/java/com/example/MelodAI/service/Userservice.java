package com.example.MelodAI.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.MelodAI.model.Credits;
import com.example.MelodAI.model.RefreshToken;
import com.example.MelodAI.model.Users;
import com.example.MelodAI.repository.RefreshTokenRepository;
import com.example.MelodAI.repository.creditsRepository;
import com.example.MelodAI.repository.usersRepository;
import com.example.MelodAI.security.jwtUtil;

import jakarta.transaction.Transactional;

@Service
public class Userservice { // Fixed class name convention

    @Autowired
    private usersRepository usersRepository;

    @Autowired
    private creditsRepository creditsRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private jwtUtil jwtUtil;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final long refreshTokenExpirationMs = 9000000;

    @Transactional
    public Users registerUser(Users users){
        if (usersRepository.findByUsername(users.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        if (usersRepository.findByEmail(users.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered!");
        }

        // Hash password
        users.setPassword(passwordEncoder.encode(users.getPassword()));

        Users savedUser = usersRepository.save(users);

        Credits initialCredits = new Credits();
        initialCredits.setUser(savedUser);
        initialCredits.setCredits(10);

        creditsRepository.save(initialCredits);

        return savedUser;
    }

    @Transactional
    public Map<String, String> loginUser(String username, String password){
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if(passwordEncoder.matches(password, user.getPassword())){
            String accessToken = jwtUtil.generateAccessToken(username);
            RefreshToken refreshToken = generateRefreshToken(user);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken.getToken());
            response.put("username", username);

            return response;
        } else {
            throw new RuntimeException("Invalid username or password!");
        }
    }

    // this function is used in login only
    @Transactional
    public RefreshToken generateRefreshToken(Users user){
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(jwtUtil.generateRefreshToken(user.getUsername()));
        refreshToken.setExpirationDate(Instant.now().plusMillis(refreshTokenExpirationMs));

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public Map<String, String> refreshAccessToken(String requestRefreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found!"));

        if (token.getExpirationDate().isBefore(Instant.now()) || !jwtUtil.validateToken(requestRefreshToken)) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token has expired. Please log in again.");
        }

        String newAccessToken = jwtUtil.generateAccessToken(token.getUser().getUsername());

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", requestRefreshToken);
        return response;
    }
}