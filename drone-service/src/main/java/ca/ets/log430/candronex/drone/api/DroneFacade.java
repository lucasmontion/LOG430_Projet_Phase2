package ca.ets.log430.candronex.drone.api;

import ca.ets.log430.candronex.drone.application.DroneService;
import ca.ets.log430.candronex.drone.domain.Drone;
import ca.ets.log430.candronex.drone.domain.DroneIntrouvableException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Interface publique du module drone : seul point d'entrée pour les autres modules.
 * Utilisée par le module commande (UC-04) pour vérifier qu'un drone existe,
 * appartient au client et est actif.
 */
@Component
public class DroneFacade {

    private final DroneService droneService;

    public DroneFacade(DroneService droneService) {
        this.droneService = droneService;
    }

    /** Vide si le drone n'existe pas OU appartient à un autre client (non-divulgation). */
    public Optional<DroneRefDto> trouverDrone(String clientId, String droneId) {
        try {
            Drone drone = droneService.consulter(clientId, droneId);
            return Optional.of(new DroneRefDto(drone.id().valeur(), drone.statut().name()));
        } catch (DroneIntrouvableException e) {
            return Optional.empty();
        }
    }
}
