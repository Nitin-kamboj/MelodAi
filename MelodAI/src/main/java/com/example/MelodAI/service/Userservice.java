package com.example.MelodAI.service;

import com.example.MelodAI.model.Credits;
import com.example.MelodAI.model.Users;
import com.example.MelodAI.repository.creditsRepository;
import com.example.MelodAI.repository.usersRepository;
import com.example.MelodAI.security.jwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

            String token = jwtUtil.generateToken(username);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("username", username);

            return response;
        }
        else {
            throw new RuntimeException("Invalid username or password!");
        }
    }
}
