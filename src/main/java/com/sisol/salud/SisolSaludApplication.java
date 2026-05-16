package com.sisol.salud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SisolSaludApplication {

	public static void main(String[] args) {
		SpringApplication.run(SisolSaludApplication.class, args);
	}

}
