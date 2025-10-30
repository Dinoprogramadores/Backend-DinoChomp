package edu.escuelaing.dinochomp_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DinoChompApplication {

    public static void main(String[] args) {
        SpringApplication.run(DinoChompApplication.class, args);
    }
}
