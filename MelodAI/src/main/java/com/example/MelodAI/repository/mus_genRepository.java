package com.example.MelodAI.repository;

import com.example.MelodAI.model.Music_Generated;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface mus_genRepository extends JpaRepository<Music_Generated, Long> {

    List<Music_Generated> findByUserId(Long userId);
}
