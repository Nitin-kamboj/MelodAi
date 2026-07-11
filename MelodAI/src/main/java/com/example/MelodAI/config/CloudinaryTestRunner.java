package com.example.MelodAI.config;

import com.example.MelodAI.service.CloudinaryStorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.net.URL;

@Component
public class CloudinaryTestRunner implements CommandLineRunner {

    private final CloudinaryStorageService cloudinaryStorageService;

    public CloudinaryTestRunner(CloudinaryStorageService cloudinaryStorageService) {
        this.cloudinaryStorageService = cloudinaryStorageService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🧪 --- STARTING CLOUDINARY DUMMY CHECK ---");

        // Using a public, safe 1-second silent MP3 file structure for the test
        String sampleAudioUrl = "https://actions.google.com/sounds/v1/alarms/digital_watch_alarm_long.ogg";

        try (InputStream inputStream = new URL(sampleAudioUrl).openStream()) {
            // Read the real audio structural data into a byte array
            byte[] mockAudioBytes = inputStream.readAllBytes();
            String testTitle = "Test Verification Track";

            System.out.println("📤 Sending valid audio structure to Cloudinary...");

            // Fire the upload call
            String testResultUrl = cloudinaryStorageService.uploadAudioBytes(mockAudioBytes, testTitle);

            System.out.println("✅ DUMMY CHECK SUCCESSFUL!");
            System.out.println("🔗 Live Test Asset URL: " + testResultUrl);

        } catch (Exception e) {
            System.err.println("❌ DUMMY CHECK FAILED!");
            System.err.println("Reason: " + e.getMessage());
        }

        System.out.println("🧪 --- CLOUDINARY DUMMY CHECK COMPLETE ---");
    }
}