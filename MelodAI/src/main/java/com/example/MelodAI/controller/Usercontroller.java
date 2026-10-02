package com.example.MelodAI.controller;

import com.example.MelodAI.model.RefreshToken;
import com.example.MelodAI.model.Users;
import com.example.MelodAI.repository.RefreshTokenRepository;
import com.example.MelodAI.repository.usersRepository;
import com.example.MelodAI.service.Userservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;



@RestController
@RequestMapping("/api/users")
public class Usercontroller {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private usersRepository usersRepository;
    @Autowired
    Userservice userservice; // Depdendecy injection (I have used autowired so I don't have to use(but this is not best way)......( public void setUserRepository(UserRepository userRepository) {     this.userRepository = userRepository;})
    RefreshToken refreshToken;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Users user) {
        try {
            Users registeredUser = userservice.registerUser(user);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Returns a clean error message if username/email is taken
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest){
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            Map<String, String> response = userservice.loginUser(username, password);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (RuntimeException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody Map<String, String> request){
        String username = request.get("username");

        try {
            if (username == null || username.isEmpty()) {
                return new ResponseEntity<>(Map.of("error", "Username is required for logout!"), HttpStatus.BAD_REQUEST);
            }
            Users user = usersRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found!"));
            refreshTokenRepository.deleteByUser(user);
            return ResponseEntity.ok(Map.of("message", "Logged out successfully!"));
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request){
        String refreshToken = request.get("refreshToken");
        try {
            if (refreshToken == null || refreshToken.isEmpty()) {
                return new ResponseEntity<>(Map.of("error", "Refresh token is required!"), HttpStatus.BAD_REQUEST);
            }
            Map<String, String> response = userservice.refreshAccessToken(refreshToken);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }
}
