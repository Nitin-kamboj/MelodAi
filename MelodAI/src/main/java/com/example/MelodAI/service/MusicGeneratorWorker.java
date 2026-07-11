package com.example.MelodAI.service;

import com.example.MelodAI.model.Music_Generated;
import com.example.MelodAI.repository.mus_genRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
//@RequiredArgsConstructor
public class MusicGeneratorWorker {
    private final mus_genRepository musGenRepository;
    private final AiGenerstionService aiGenerationService; // Inject the AI block

    public MusicGeneratorWorker(mus_genRepository musGenRepository, AiGenerstionService aiGenerationService) {
        this.musGenRepository = musGenRepository;
        this.aiGenerationService = aiGenerationService;
    }

    @Async
    public void generateMusicAsync(Music_Generated track) {
        try {
            // 1. Find the track blueprint details
//            Thread.sleep(2000);
//            Music_Generated track = musGenRepository.findById(trackId)
//                    .orElseThrow(() -> new RuntimeException("Track not found"));

            // 2. Call the live AI generation endpoint using the user's specific prompt text
            System.out.println("🚀 Forwarding prompt to Meta MusicGen AI: " + track.getTitle());
            String realAudioUrl = aiGenerationService.generateAudioUrl(track.getTitle());

            if (realAudioUrl == null) {
                throw new RuntimeException("AI failed to return an output file link.");
            }

            // 3. Save the actual audio resource URL directly back to your database!
            track.setCloudinaryUrl(realAudioUrl);
            track.setStatus("COMPLETED");
            musGenRepository.save(track);

            System.out.println("🎉 Success! Track ID " + track.getId() + " has been generated cleanly.");

        } catch (Exception e) {
            System.err.println("❌ Generation Error: " + e.getMessage());
                track.setStatus("FAILED");
                musGenRepository.save(track);
        }
    }
}
