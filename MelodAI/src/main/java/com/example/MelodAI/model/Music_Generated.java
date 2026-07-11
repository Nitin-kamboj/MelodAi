package com.example.MelodAI.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "music_generated")
@Data
public class Music_Generated {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private String title;

    private String status;

    @Column(name = "cloudinary_url")
    private String cloudinaryUrl;
}
