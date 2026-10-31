package ca.ets.log430.candronex.drone.adapter.in.web;

import ca.ets.log430.candronex.commun.adapter.in.web.ContratOpenApi;
import ca.ets.log430.candronex.drone.application.DroneService;
import ca.ets.log430.candronex.drone.application.EnregistrerDroneCommand;
import ca.ets.log430.candronex.drone.domain.Drone;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Drones", description = "UC-02 : enregistrer et consulter les drones du client authentifié")
@RequestMapping("/api/drone-service/v1/drone")
public class DroneController {
    private static final Logger log = LoggerFactory.getLogger(DroneController.class);

    private final DroneService droneService;

    public DroneController(DroneService droneService) {
        this.droneService = droneService;
    }

    @Operation(summary = "Enregistrer un drone (UC-02)",
            description = "Associe un drone et son identité réseau (IMSI, ICCID) au client du jeton. "
                    + "L'IMSI et l'ICCID ne sont jamais renvoyés.\n\n"
                    + "Codes d'erreur : 400 REQUETE_INVALIDE, DRONE_ID_INVALIDE, IMSI_INVALIDE, ICCID_INVALIDE, "
                    + "TYPE_SIM_INVALIDE ; 409 DRONE_DEJA_ENREGISTRE, IMSI_DEJA_UTILISE, ICCID_DEJA_UTILISE, "
                    + "CONFLIT_DONNEES.")
    @ApiResponse(responseCode = "201", description = "Drone enregistré, statut ACTIF",
            headers = @Header(name = "Location", description = "URL du drone créé"))
    @ApiResponse(responseCode = "400", ref = ContratOpenApi.REPONSES + "RequeteInvalide")
    @ApiResponse(responseCode = "401", ref = ContratOpenApi.REPONSES + "NonAuthentifie")
    @ApiResponse(responseCode = "409", ref = ContratOpenApi.REPONSES + "DroneEnConflit")
    @ApiResponse(responseCode = "500", ref = ContratOpenApi.REPONSES + "ErreurInterne")
    @PostMapping
    public ResponseEntity<DroneResponse> enregistrer(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jeton,
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

    @Operation(summary = "Consulter un drone du client (UC-02)",
            description = "Sans IMSI ni ICCID. Le drone d'un autre client donne 404, comme un drone inexistant.\n\n"
                    + "Codes d'erreur : 400 DRONE_ID_INVALIDE ; 404 DRONE_INTROUVABLE.")
    @ApiResponse(responseCode = "200", description = "Le drone")
    @ApiResponse(responseCode = "400", ref = ContratOpenApi.REPONSES + "RequeteInvalide")
    @ApiResponse(responseCode = "401", ref = ContratOpenApi.REPONSES + "NonAuthentifie")
    @ApiResponse(responseCode = "404", ref = ContratOpenApi.REPONSES + "DroneIntrouvable")
    @ApiResponse(responseCode = "500", ref = ContratOpenApi.REPONSES + "ErreurInterne")
    @GetMapping("/{droneId}")
    public DroneResponse consulter(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jeton,
            @Parameter(description = "Identifiant du drone choisi par le client", example = "DRONE-600")
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
    @Operation(summary = "Identité réseau d'un drone (interne, UC-06)", tags = "Interne",
            description = "Appelée par slice-service au moment de l'activation. Absente de la passerelle. "
                    + "Seule réponse qui contient l'IMSI ; toujours filtrée par le client du jeton.\n\n"
                    + "Codes d'erreur : 400 DRONE_ID_INVALIDE ; 404 DRONE_INTROUVABLE.")
    @ApiResponse(responseCode = "200", description = "Identité réseau du drone")
    @ApiResponse(responseCode = "400", ref = ContratOpenApi.REPONSES + "RequeteInvalide")
    @ApiResponse(responseCode = "401", ref = ContratOpenApi.REPONSES + "NonAuthentifie")
    @ApiResponse(responseCode = "404", ref = ContratOpenApi.REPONSES + "DroneIntrouvable")
    @ApiResponse(responseCode = "500", ref = ContratOpenApi.REPONSES + "ErreurInterne")
    @GetMapping("/{droneId}/identite-reseau")
    public IdentiteReseauResponse identiteReseau(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jeton,
            @Parameter(description = "Identifiant du drone", example = "DRONE-600")
            @PathVariable String droneId) {
        String clientId = jeton.getClaimAsString("azp");
        log.info("[adapter.in] GET /api/drone-service/v1/drone/{}/identite-reseau clientId={}", droneId, clientId);
        return IdentiteReseauResponse.depuis(droneService.consulter(clientId, droneId));
    }
}
