package com.fsse2510.fsse2510_project_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableJpaRepositories(basePackages = "com.fsse2510.fsse2510_project_backend.repository")
@EnableAsync
@EnableScheduling
public class Fsse2510ProjectBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Fsse2510ProjectBackendApplication.class, args);
    }

}
