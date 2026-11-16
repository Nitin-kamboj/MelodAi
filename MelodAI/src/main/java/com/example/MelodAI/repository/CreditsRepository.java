package com.example.MelodAI.repository;

import com.example.MelodAI.model.Credits;
import com.example.MelodAI.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditsRepository extends JpaRepository <Credits, Long> {

    Optional<Credits> findById(Long userId);
    Optional<Credits> findByUser(Users user);
}
