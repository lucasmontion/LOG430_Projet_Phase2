package ca.ets.log430.candronex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée du microservice slice (contextes Activation et Inventaire de services, UC-06).
 * Placé dans ca.ets.log430.candronex pour que Spring détecte les paquets slice.* et commun.*.
 */
@SpringBootApplication
public class SliceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SliceServiceApplication.class, args);
    }
}
