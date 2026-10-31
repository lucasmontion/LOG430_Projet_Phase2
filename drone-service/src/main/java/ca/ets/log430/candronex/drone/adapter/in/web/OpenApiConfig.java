package ca.ets.log430.candronex.drone.adapter.in.web;

import ca.ets.log430.candronex.commun.adapter.in.web.ContratOpenApi;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Contrat OpenAPI de drone-service (servi à /v3/api-docs, exporté dans openapi/drone-service.yaml). */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI contratDroneService() {
        OpenAPI contrat = ContratOpenApi.base("drone-service",
                "Contexte Gestion des drones (UC-02). Les routes marquées « Interne » ne sont pas exposées "
                        + "par la passerelle.",
                "http://drone-service:8081");
        var composants = contrat.getComponents();
        ContratOpenApi.erreur(composants, "DroneIntrouvable", 404,
                "Drone inexistant OU appartenant à un autre client (indiscernables). Code : DRONE_INTROUVABLE",
                "DRONE_INTROUVABLE", "Drone DRONE-600 introuvable");
        ContratOpenApi.erreur(composants, "DroneEnConflit", 409,
                "Codes : DRONE_DEJA_ENREGISTRE (même droneId pour ce client), IMSI_DEJA_UTILISE, ICCID_DEJA_UTILISE "
                        + "(uniques dans tout le système), CONFLIT_DONNEES (doublon détecté par la base)",
                "IMSI_DEJA_UTILISE", "Cet IMSI est déjà associé à un drone");
        return contrat;
    }
}
