package com.example.MelodAI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MelodAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MelodAiApplication.class, args);
	}

}
