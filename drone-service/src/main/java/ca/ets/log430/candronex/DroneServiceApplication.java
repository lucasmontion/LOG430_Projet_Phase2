package ca.ets.log430.candronex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée du microservice drone (contexte Gestion des drones, UC-02).
 */
@SpringBootApplication
public class DroneServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DroneServiceApplication.class, args);
    }
}