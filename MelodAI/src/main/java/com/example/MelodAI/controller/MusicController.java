package com.example.MelodAI.controller;

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

@RestController
@RequestMapping("/api/music")
public class MusicController {
    @Autowired
    MusicService musicService;    // IOC (Inversion of control...  Dependency injection)

    // generate the music from the lyria(Google AI)
    @PostMapping("/generate_music")
    public ResponseEntity<?> generateMusic(@RequestParam Long userId, @RequestParam String prompt){
        try {
            Music_Generated track = musicService.musicGenerated(userId, prompt);
            return new ResponseEntity<>(track, HttpStatus.ACCEPTED);
        }catch (RuntimeException e){
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

    // 2. Fetch the entire song history for a user
    @GetMapping("/music/{userId}")
    public ResponseEntity<java.util.List<Music_Generated>> getUserLibrary(@PathVariable Long userId) {
        return ResponseEntity.ok(musicService.getUserMusicLibrary(userId));
    }
}
