package com.example.MelodAI.service;
import com.cloudinary.Cloudinary;

import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryStorageService {

    private final Cloudinary cloudinary;
    public String uploadAudioBytes(byte[] audioBytes, String trackTitle){
        try{
            Map<?, ?> uploadResult = cloudinary.uploader().upload(audioBytes, ObjectUtils.asMap(
                    "resource_type", "video", // Cloudinary processes audio files under the 'video' engine
                    "public_id", "melodai_" + System.currentTimeMillis() + "_" + trackTitle.replaceAll("[^a-zA-Z0-9]", "")
            ));
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
