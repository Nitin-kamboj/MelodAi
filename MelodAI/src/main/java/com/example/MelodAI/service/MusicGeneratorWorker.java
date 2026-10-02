package com.example.MelodAI.service;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.MelodAI.model.Music_Generated;
import com.example.MelodAI.repository.mus_genRepository;
import com.google.genai.Client;
import com.google.genai.gaos.models.interactions.AudioContent;
import com.google.genai.gaos.models.interactions.Content;
import com.google.genai.gaos.models.interactions.CreateModelInteraction;
import com.google.genai.gaos.models.interactions.Interaction;
import com.google.genai.gaos.models.interactions.InteractionsInput;
import com.google.genai.gaos.models.interactions.Model;
import com.google.genai.gaos.models.interactions.ModelOutputStep;
import com.google.genai.gaos.models.interactions.Step;
import com.google.genai.gaos.models.operations.CreateInteractionRequestBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MusicGeneratorWorker {

    private final mus_genRepository musGenRepository;
    private final Cloudinary cloudinary;

    @Value("${google.gemini.api-key}")
    private String apiKey;

    @Async
    public void generateMusicAsync(Long trackId, String title) {

        Music_Generated track = musGenRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Track not found: " + trackId));

        try {
            // --------------------------------------------------
            // 1. Mark track as PROCESSING
            // --------------------------------------------------
            track.setStatus("PROCESSING");
            musGenRepository.save(track);
            log.info("Started music generation for track ID: {}", trackId);

            // --------------------------------------------------
            // 2. Create Gemini client
            // --------------------------------------------------
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            log.info("Gemini client created");

            // --------------------------------------------------
            // 3. Build Lyria request
            // --------------------------------------------------
            CreateModelInteraction params = CreateModelInteraction.builder()
                    .model(Model.of("lyria-3-clip-preview"))
                    .input(InteractionsInput.of(
                            title))
                    .build();

            log.info("Sending request to Lyria...");

            // --------------------------------------------------
            // 4. Generate music
            // --------------------------------------------------
            Interaction interaction = client.interactions
                    .create(CreateInteractionRequestBody.of(params))
                    .interaction()
                    .get();

            log.info("Lyria request completed");

            // --------------------------------------------------
            // 5. Extract generated audio bytes from interaction steps
            // --------------------------------------------------
            byte[] audioBytes = null;

            if (interaction.steps().isPresent()) {
                for (Step step : interaction.steps().get()) {
                    if (step instanceof ModelOutputStep outputStep) {
                        if (outputStep.content().isPresent()) {
                            for (Content contentBlock : outputStep.content().get()) {
                                if (contentBlock instanceof AudioContent audioBlock) {
                                    if (audioBlock.data().isPresent()) {
                                        audioBytes = Base64.getDecoder().decode(audioBlock.data().get());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fallback to convenience method if step parsing yields no bytes
            if ((audioBytes == null || audioBytes.length == 0) && interaction.outputAudio().isPresent()) {
                if (interaction.outputAudio().get().data().isPresent()) {
                    audioBytes = Base64.getDecoder().decode(interaction.outputAudio().get().data().get());
                }
            }

            if (audioBytes == null || audioBytes.length == 0) {
                log.error("Lyria returned NO AUDIO data for track ID: {}", trackId);
                log.info("Lyria output text: {}", interaction.outputText().orElse("NO TEXT"));

                track.setStatus("FAILED");
                musGenRepository.save(track);
                return;
            }

            log.info("Audio decoded successfully. Size: {} bytes", audioBytes.length);

            // --------------------------------------------------
            // 6. Upload MP3 to Cloudinary
            // --------------------------------------------------
            log.info("Uploading audio to Cloudinary...");

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    audioBytes,
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "folder", "melodai/music",
                            "format", "mp3"
                    )
            );

            // --------------------------------------------------
            // 7. Extract Cloudinary URL & Save entity
            // --------------------------------------------------
            String audioUrl = (String) uploadResult.get("secure_url");

            if (audioUrl == null || audioUrl.isBlank()) {
                log.error("Cloudinary returned no secure URL for track ID: {}", trackId);
                track.setStatus("FAILED");
                musGenRepository.save(track);
                return;
            }

            log.info("Cloudinary upload successful: {}", audioUrl);

            track.setFilePath(audioUrl);
            track.setStatus("COMPLETED");
            musGenRepository.save(track);

            log.info("Music generation COMPLETED for track ID: {}", trackId);

        } catch (IOException e) {
            log.error("Cloudinary upload failed for track ID: {}", trackId, e);
            track.setStatus("FAILED");
            musGenRepository.save(track);
        } catch (Exception e) {
            log.error("Music generation failed for track ID: {}", trackId, e);
            track.setStatus("FAILED");
            musGenRepository.save(track);
        }
    }
}