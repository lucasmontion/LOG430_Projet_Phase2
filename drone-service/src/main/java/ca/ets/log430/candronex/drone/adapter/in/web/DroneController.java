package ca.ets.log430.candronex.drone.adapter.in.web;

import ca.ets.log430.candronex.drone.application.DroneService;
import ca.ets.log430.candronex.drone.application.EnregistrerDroneCommand;
import ca.ets.log430.candronex.drone.domain.Drone;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.net.URI;

/**
 * Adaptateur d'entrée REST : traduit HTTP <-> application. Aucune règle métier ici.
 * Le client est identifié par la claim azp du JWT, vérifié par Spring Security (SecuriteConfig).
 */
@RestController
@RequestMapping("/api/drone-service/v1/drone")
public class DroneController {
    private static final Logger log = LoggerFactory.getLogger(DroneController.class);

    private final DroneService droneService;

    public DroneController(DroneService droneService) {
        this.droneService = droneService;
    }

    @PostMapping
    public ResponseEntity<DroneResponse> enregistrer(
            @AuthenticationPrincipal Jwt jeton,
            @Valid @RequestBody EnregistrerDroneRequest requete) {
        String clientId = jeton.getClaimAsString("azp");   // identité du client : claim azp du jeton VÉRIFIÉ
        log.info("[adapter.in] POST /api/drone-service/v1/drone clientId={} droneId={}", clientId, requete.droneId());

        Drone drone = droneService.enregistrer(new EnregistrerDroneCommand(
                clientId, requete.droneId(), requete.imsi(), requete.iccid(), requete.typeSim()));

        log.info("[adapter.in] POST /api/drone-service/v1/drone -> 201 droneId={}", drone.id().valeur());
        return ResponseEntity
                .created(URI.create("/api/drone-service/v1/drone/" + drone.id().valeur()))
                .body(DroneResponse.depuis(drone));
    }

    @GetMapping("/{droneId}")
    public DroneResponse consulter(
            @AuthenticationPrincipal Jwt jeton,
            @PathVariable String droneId) {
        String clientId = jeton.getClaimAsString("azp");   // identité du client : claim azp du jeton VÉRIFIÉ
        log.info("[adapter.in] GET /api/drone-service/v1/drone/{} clientId={}", droneId, clientId);
        return DroneResponse.depuis(droneService.consulter(clientId, droneId));
    }

    /**
     * Route INTERNE (UC-06) : identité réseau du drone, demandée par slice-service au moment de l'activation.
     * Absente de krakend.json, donc injoignable depuis l'extérieur ; toujours filtrée par le client du jeton.
     * C'est la seule réponse qui contient l'IMSI.
     */
    @GetMapping("/{droneId}/identite-reseau")
    public IdentiteReseauResponse identiteReseau(
            @AuthenticationPrincipal Jwt jeton,
            @PathVariable String droneId) {
        String clientId = jeton.getClaimAsString("azp");
        log.info("[adapter.in] GET /api/drone-service/v1/drone/{}/identite-reseau clientId={}", droneId, clientId);
        return IdentiteReseauResponse.depuis(droneService.consulter(clientId, droneId));
    }
}
