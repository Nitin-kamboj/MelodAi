package com.example.MelodAI.controller;

import com.example.MelodAI.model.Users;
import com.example.MelodAI.repository.usersRepository;
import org.springframework.security.core.Authentication; // 👈 Fixed import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.MelodAI.model.Music_Generated;
import com.example.MelodAI.service.MusicService;

import java.util.List;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    @Autowired
    private MusicService musicService;

    @Autowired // 👈 Added this so usersRepository is available
    private usersRepository usersRepository;

    // generate the music from the lyria(Google AI)
    @PostMapping("/generate_music")
    public ResponseEntity<?> generateMusic(@RequestParam String prompt, Authentication authentication) {
        try {
            // Optional: Secure this one too so frontend doesn't need to pass userId!
            String username = authentication.getName();
            Users user = usersRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

            Music_Generated track = musicService.musicGenerated(user.getId(), prompt);
            return new ResponseEntity<>(track, HttpStatus.ACCEPTED);
        } catch (RuntimeException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 1. Check the status of a specific track id
    @GetMapping("/status/{trackId}")
    public ResponseEntity<?> getTrackStatus(@PathVariable Long trackId) {
        try {
            Music_Generated track = musicService.getTrackStatus(trackId);
            return ResponseEntity.ok(track);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // 2. Fetch the entire song history for a user (by ID, if still needed)
    @GetMapping("/{userId}")
    public ResponseEntity<List<Music_Generated>> getUserLibrary(@PathVariable Long userId) {
        return ResponseEntity.ok(musicService.getUserMusicLibrary(userId));
    }

    // 3. Fetch the logged-in user's music library securely via JWT token
    @GetMapping("/my-library")
    public ResponseEntity<?> getMyMusicLibrary(Authentication authentication) {
        try {
            String username = authentication.getName();

            Users user = usersRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

            List<Music_Generated> userTracks = musicService.getUserMusicLibrary(user.getId());
            return ResponseEntity.ok(userTracks);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // Get all Musics generated (Global feed)
    @GetMapping("/all")
    public ResponseEntity<List<Music_Generated>> getAllMusic(){
        return ResponseEntity.ok(musicService.getAllMusicService());
    }
}