package com.example.MelodAI.repository;

import com.example.MelodAI.model.Credits;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface creditsRepository extends JpaRepository <Credits, Long> {

    Optional<Credits> findById(Long userId);
}
