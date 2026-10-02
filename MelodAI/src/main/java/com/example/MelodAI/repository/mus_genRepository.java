package com.example.MelodAI.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.MelodAI.model.Music_Generated;

public interface mus_genRepository extends JpaRepository<Music_Generated, Long> {

    List<Music_Generated> findByUser_Id(Long userId);

}
