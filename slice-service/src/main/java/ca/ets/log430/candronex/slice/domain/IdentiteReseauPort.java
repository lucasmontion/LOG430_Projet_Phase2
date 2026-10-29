package ca.ets.log430.candronex.slice.domain;

import ca.ets.log430.candronex.commun.domaine.ClientId;

import java.util.Optional;

/**
 * Port de sortie : résoudre droneId -> IMSI au moment de l'activation (UC-06, étape 1).
 * L'IMSI n'est jamais figé dans la commande, pour tenir compte d'un changement de SIM.
 */
public interface IdentiteReseauPort {

    /**
     * Vide si le drone n'existe pas, appartient à un autre client ou n'est plus actif (retiré, suspendu).
     * @throws IdentiteReseauIndisponibleException si la source de l'identité ne répond pas
     */
    Optional<IdentiteReseau> resoudre(ClientId clientId, DroneId droneId);
}
