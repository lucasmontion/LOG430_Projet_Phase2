package ca.ets.log430.candronex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

/**
 * Point d'entrée du microservice commande (contextes Gestion des commandes et Catalogue, UC-04).
 * Placé dans ca.ets.log430.candronex pour que Spring détecte les paquets
 * commande.*, catalogue.* et commun.*.
 */
@SpringBootApplication
public class CommandeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommandeServiceApplication.class, args);
    }

    /** Horloge injectée (acceptedAt) : le service applicatif reste testable avec une heure fixe. */
    @Bean
    Clock horloge() {
        return Clock.systemUTC();
    }
}
