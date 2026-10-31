package ca.ets.log430.candronex.commande.adapter.in.web;

import ca.ets.log430.candronex.commun.adapter.in.web.ContratOpenApi;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Contrat OpenAPI de commande-service (servi à /v3/api-docs, exporté dans openapi/commande-service.yaml). */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI contratCommandeService() {
        OpenAPI contrat = ContratOpenApi.base("commande-service",
                "Contexte Commandes (UC-04) et catalogue des services. Une commande acceptée déclenche "
                        + "l'activation de chaque élément (UC-06) dans slice-service.",
                "http://commande-service:8082");
        var composants = contrat.getComponents();
        ContratOpenApi.erreur(composants, "Introuvable", 404,
                "Ressource inexistante OU appartenant à un autre client (indiscernables). "
                        + "Codes : COMMANDE_INTROUVABLE, DRONE_INTROUVABLE",
                "COMMANDE_INTROUVABLE", "Commande 6f1c2a9e-0b7d-4c3e-9a51-8d2e4f6b1a07 introuvable");
        ContratOpenApi.erreur(composants, "CleReutilisee", 409,
                "Même Idempotency-Key qu'une commande existante, mais contenu différent. "
                        + "Codes : CLE_IDEMPOTENCE_REUTILISEE, CONFLIT_DONNEES",
                "CLE_IDEMPOTENCE_REUTILISEE",
                "Cette clé d'idempotence a déjà servi pour une commande au contenu différent");
        ContratOpenApi.erreur(composants, "RegleMetier", 422,
                "Requête bien formée, mais refusée par une règle métier. "
                        + "Codes : DRONE_NON_ELIGIBLE, SERVICE_INCONNU, ELEMENT_EN_DOUBLE",
                "DRONE_NON_ELIGIBLE", "Le drone DRONE-600 n'est pas actif (statut SUSPENDU)");
        ContratOpenApi.erreur(composants, "DependanceIndisponible", 503,
                "drone-service ne répond pas : la commande n'est pas créée et peut être renvoyée avec la même clé. "
                        + "Code : DRONE_SERVICE_INDISPONIBLE",
                "DRONE_SERVICE_INDISPONIBLE", "Le service des drones est momentanément indisponible");
        return contrat;
    }
}
