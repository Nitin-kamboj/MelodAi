
package com.example.MelodAI.controller;

import com.example.MelodAI.model.Users;
import com.example.MelodAI.service.Userservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class Usercontroller {

    @Autowired
    Userservice userservice;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Users user) {
        try {
            Users registeredUser = userservice.registerUser(user);

            return new ResponseEntity<>(
                    registeredUser,
                    HttpStatus.CREATED
            );

        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> loginRequest) {

        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            Map<String, String> response =
                    userservice.loginUser(username, password);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(
            Authentication authentication) {

        try {
            userservice.logoutUser(authentication);

            return ResponseEntity.ok(
                    Map.of("message", "Logged out successfully!")
            );

        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    Map.of("error", e.getMessage()),
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        try {
            if (refreshToken == null || refreshToken.isEmpty()) {
                return new ResponseEntity<>(
                        Map.of("error", "Refresh token is required!"),
                        HttpStatus.BAD_REQUEST
                );
            }

            Map<String, String> response =
                    userservice.refreshAccessToken(refreshToken);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    Map.of("error", e.getMessage()),
                    HttpStatus.UNAUTHORIZED
            );
        }
    }
}
