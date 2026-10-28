package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.ClientId;

import java.util.Optional;

/**
 * Port de sortie : ce dont la commande a besoin de savoir sur un drone (UC-04).
 * Défini par le contexte Commandes, dans son vocabulaire ; l'adaptateur décide comment l'obtenir.
 */
public interface VerificationDronePort {

    /** Vide si le drone n'existe pas OU appartient à un autre client (non-divulgation). */
    Optional<DroneVerifie> verifier(ClientId clientId, DroneId droneId);
}