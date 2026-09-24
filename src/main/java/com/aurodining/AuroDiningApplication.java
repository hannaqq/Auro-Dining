package com.aurodining;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableCaching
@EnableJpaAuditing
public class AuroDiningApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuroDiningApplication.class, args);
		log.info("project started");
	}

}
