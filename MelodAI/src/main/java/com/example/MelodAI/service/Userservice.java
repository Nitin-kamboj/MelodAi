package com.example.MelodAI.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.example.MelodAI.model.RefreshToken;
import com.example.MelodAI.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.MelodAI.model.Credits;
import com.example.MelodAI.model.Users;
import com.example.MelodAI.repository.creditsRepository;
import com.example.MelodAI.repository.usersRepository;
import com.example.MelodAI.security.jwtUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;

@Service
public class Userservice {

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

    private long refreshTokenExpirationMs= 9000000;

    @Transactional
    public Users registerUser(Users users){
        Optional<Users> existingUser = usersRepository.findByUsername(users.getUsername());

        if (existingUser.isPresent()) {
            // If true, the user exists in the database!
            throw new RuntimeException("Username is already taken!");
        }

        Optional<Users> existingEmail = usersRepository.findByEmail(users.getEmail());
        if (existingEmail.isPresent()) {
            throw new RuntimeException("Email is already registered!");
        }

        users.setPassword(passwordEncoder.encode(users.getPassword()));

        Users savedUser = usersRepository.save((users));
        Credits initialCredits = new Credits();
        initialCredits.setUser(savedUser); // Connects the 1:1 link
        initialCredits.setCredits(10);     // Grants 10 startup points

        creditsRepository.save(initialCredits);

        return savedUser;
    }

    @Transactional
    public Map<String, String> loginUser(String username, String password){
        Optional <Users> existingUser = usersRepository.findByUsername(username);
        if(existingUser.isEmpty()){
            throw new RuntimeException("User not Found!");
        }

        if(passwordEncoder.matches(password , existingUser.get().getPassword())){
            Users user = usersRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found!"));
            String accessToken = jwtUtil.generateAccessToken(username);
            RefreshToken refreshToken = generateRefreshToken(user);
//            AccessToken accessToken =
            Map<String, String> response = new HashMap<>();
            response.put("RefreshToken", refreshToken.getToken());
            response.put("username", username);
            response.put("AccessToken", accessToken);

            return response;
        }
        else {
            throw new RuntimeException("Invalid username or password!");
        }
    }

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
