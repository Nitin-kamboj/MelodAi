package com.example.MelodAI.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class Users {
    public enum Role {
        USER,
        ADMIN
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    private long id;

    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
}
