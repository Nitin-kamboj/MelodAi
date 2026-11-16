package com.example.MelodAI.controller;

import com.example.MelodAI.model.Credits;
import com.example.MelodAI.model.Users;
import com.example.MelodAI.repository.CreditsRepository;
import com.example.MelodAI.repository.usersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/credits")
public class CreditsController {

    @Autowired
    private usersRepository userRepository;

    @Autowired
    private CreditsRepository creditsRepository;

    @GetMapping
    public ResponseEntity<?> getUserCredits(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String username = authentication.getName();

        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch credits from the separate credits table using the user
        Credits creditsRecord = creditsRepository.findByUser(user)
                .orElseGet(() -> {
                    // Fallback safety net: if a user row exists but somehow lacks a credits row, initialize it
                    Credits newCredits = new Credits();
                    newCredits.setUser(user);
                    newCredits.setCredits(0); // default value
                    return creditsRepository.save(newCredits);
                });

        return ResponseEntity.ok(Map.of("credits", creditsRecord.getCredits()));
    }
}