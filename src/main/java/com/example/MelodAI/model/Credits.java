package com.example.MelodAI.model;
//import com.example.MelodAI.model.Users;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "credits")
public class Credits {
    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(nullable = false)
    private Integer credits = 10;
}
