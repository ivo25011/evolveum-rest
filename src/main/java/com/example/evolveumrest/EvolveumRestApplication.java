package com.example.evolveumrest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EvolveumRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(EvolveumRestApplication.class, args);
	}

}
