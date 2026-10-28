package ca.ets.log430.candronex.drone.adapter.in.web;

import ca.ets.log430.candronex.drone.application.DroneService;
import ca.ets.log430.candronex.drone.application.EnregistrerDroneCommand;
import ca.ets.log430.candronex.drone.domain.Drone;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Adaptateur d'entrée REST : traduit HTTP <-> application. Aucune règle métier ici.
 * Le client s'identifie par l'en-tête X-Client-Id (UC-01 hors Phase 1).
 */
@RestController
@RequestMapping("/api/drone-service/v1/drone")
public class DroneController {
    private static final Logger log = LoggerFactory.getLogger(DroneController.class);
    private static final String EN_TETE_CLIENT = "X-Client-Id";

    private final DroneService droneService;

    public DroneController(DroneService droneService) {
        this.droneService = droneService;
    }

    @PostMapping
    public ResponseEntity<DroneResponse> enregistrer(
            @RequestHeader(EN_TETE_CLIENT) String clientId,
            @Valid @RequestBody EnregistrerDroneRequest requete) {
        log.info("[adapter.in] POST /api/drone-service/v1/drones clientId={} droneId={}", clientId, requete.droneId());

        Drone drone = droneService.enregistrer(new EnregistrerDroneCommand(
                clientId, requete.droneId(), requete.imsi(), requete.iccid(), requete.typeSim()));

        log.info("[adapter.in] POST /api/drone-service/v1/drone -> 201 droneId={}", drone.id().valeur());
        return ResponseEntity
                .created(URI.create("/api/drone-service/v1/drone/" + drone.id().valeur()))
                .body(DroneResponse.depuis(drone));
    }

    @GetMapping("/{droneId}")
    public DroneResponse consulter(
            @RequestHeader(EN_TETE_CLIENT) String clientId,
            @PathVariable String droneId) {
        log.info("[adapter.in] GET /api/v1/drones/{} clientId={}", droneId, clientId);
        return DroneResponse.depuis(droneService.consulter(clientId, droneId));
    }
}
