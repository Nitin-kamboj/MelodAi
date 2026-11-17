package com.example.MelodAI.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;
@Entity
@Table(name = "RefreshToken")
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( unique = true)
    private String token;

    @Column()
    private Instant expirationDate;

    @OneToOne
    @JoinColumn(name = "Users", referencedColumnName = "id")
    private Users user;
}
