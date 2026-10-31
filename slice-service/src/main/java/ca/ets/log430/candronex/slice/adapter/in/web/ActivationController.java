package ca.ets.log430.candronex.slice.adapter.in.web;

import ca.ets.log430.candronex.commun.adapter.in.web.ContratOpenApi;
import ca.ets.log430.candronex.slice.application.ActivationService;
import ca.ets.log430.candronex.slice.application.DemanderActivationCommand;
import ca.ets.log430.candronex.slice.application.ResultatDemande;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Adaptateur d'entrée REST de UC-06. Le client est identifié par la claim azp du JWT vérifié.
 * - POST /activation et GET /service?commandeId= : routes INTERNES (commande-service), absentes de KrakenD.
 * - GET /service/{serviceId} : route publique (consulter l'état d'un service).
 */
@RestController
@Tag(name = "Services", description = "UC-06 : état des services réseau activés pour le client authentifié")
@RequestMapping("/api/slice-service/v1")
public class ActivationController {
    private static final Logger log = LoggerFactory.getLogger(ActivationController.class);

    private final ActivationService activationService;

    public ActivationController(ActivationService activationService) {
        this.activationService = activationService;
    }

    /** 202 : demande acceptée, activation en arrière-plan. 200 : rejeu, service existant. */
    @Operation(summary = "Demander l'activation d'un élément de commande (interne, UC-06)", tags = "Interne",
            description = "Appelée par commande-service après l'enregistrement d'une commande. Absente de la passerelle. "
                    + "Crée le service (EN_ATTENTE) puis l'active en arrière-plan : identité réseau demandée à "
                    + "drone-service, puis provisionnement auprès du réseau 5G. Idempotente : un seul service par "
                    + "(commandeId, elementId).\n\n"
                    + "Codes d'erreur : 400 REQUETE_INVALIDE, DRONE_ID_INVALIDE, SST_INVALIDE, DNN_INVALIDE, "
                    + "FIVEQI_INVALIDE, ARP_INVALIDE, AMBR_INVALIDE.")
    @ApiResponse(responseCode = "202", description = "Demande acceptée ; suivre l'état du service à l'URL Location",
            headers = @Header(name = "Location", description = "URL du service"))
    @ApiResponse(responseCode = "200", description = "Rejeu : le service existait déjà pour cet élément")
    @ApiResponse(responseCode = "400", ref = ContratOpenApi.REPONSES + "RequeteInvalide")
    @ApiResponse(responseCode = "401", ref = ContratOpenApi.REPONSES + "NonAuthentifie")
    @ApiResponse(responseCode = "500", ref = ContratOpenApi.REPONSES + "ErreurInterne")
    @PostMapping("/activation")
    public ResponseEntity<ServiceResponse> demander(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jeton,
            @Valid @RequestBody DemandeActivationRequest requete) {
        String clientId = jeton.getClaimAsString("azp");
        log.info("[adapter.in] POST /api/slice-service/v1/activation clientId={} commandeId={} elementId={}",
                clientId, requete.commandeId(), requete.elementId());

        var c = requete.caracteristiques();
        ResultatDemande resultat = activationService.demander(new DemanderActivationCommand(
                clientId, requete.commandeId(), requete.elementId(), requete.droneId(), requete.typeService(),
                requete.acceptedAt(),
                c.sst(), c.sd(), c.dnn(), c.fiveQi(), c.arp(), c.ambrMontant(), c.ambrDescendant()));

        ServiceResponse corps = ServiceResponse.depuis(resultat.service());
        if (!resultat.nouvelle()) {
            log.info("[adapter.in] POST activation -> 200 (rejeu) serviceId={}", corps.serviceId());
            return ResponseEntity.ok(corps);
        }
        log.info("[adapter.in] POST activation -> 202 serviceId={}", corps.serviceId());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .location(URI.create("/api/slice-service/v1/service/" + corps.serviceId()))
                .body(corps);
    }

    @Operation(summary = "Consulter l'état d'un service du client (UC-06)",
            description = "Le service d'un autre client donne 404, comme un service inexistant. Jamais d'IMSI.\n\n"
                    + "Codes d'erreur : 400 SERVICE_ID_INVALIDE ; 404 SERVICE_INTROUVABLE.")
    @ApiResponse(responseCode = "200", description = "Le service et son état")
    @ApiResponse(responseCode = "400", ref = ContratOpenApi.REPONSES + "RequeteInvalide")
    @ApiResponse(responseCode = "401", ref = ContratOpenApi.REPONSES + "NonAuthentifie")
    @ApiResponse(responseCode = "404", ref = ContratOpenApi.REPONSES + "ServiceIntrouvable")
    @ApiResponse(responseCode = "500", ref = ContratOpenApi.REPONSES + "ErreurInterne")
    @GetMapping("/service/{serviceId}")
    public ServiceResponse consulter(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jeton,
            @Parameter(description = "Identifiant du service (UUID)", example = "4d8e2b1f-7a3c-4e95-8b06-1c9f5a2d7e40")
            @PathVariable String serviceId) {
        String clientId = jeton.getClaimAsString("azp");
        log.info("[adapter.in] GET /api/slice-service/v1/service/{} clientId={}", serviceId, clientId);
        return ServiceResponse.depuis(activationService.consulter(clientId, serviceId));
    }

    @Operation(summary = "Services d'une commande (interne, UC-04)", tags = "Interne",
            description = "Appelée par commande-service pour actualiser l'état des éléments. Absente de la passerelle. "
                    + "Liste vide si la commande n'a aucun service, ou appartient à un autre client.\n\n"
                    + "Codes d'erreur : 400 REQUETE_INVALIDE (commandeId absent ou qui n'est pas un UUID).")
    @ApiResponse(responseCode = "200", description = "Les services de la commande (éventuellement aucun)")
    @ApiResponse(responseCode = "400", ref = ContratOpenApi.REPONSES + "RequeteInvalide")
    @ApiResponse(responseCode = "401", ref = ContratOpenApi.REPONSES + "NonAuthentifie")
    @ApiResponse(responseCode = "500", ref = ContratOpenApi.REPONSES + "ErreurInterne")
    @GetMapping("/service")
    public List<ServiceResponse> servicesDeLaCommande(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jeton,
            @Parameter(description = "Identifiant de la commande", example = "6f1c2a9e-0b7d-4c3e-9a51-8d2e4f6b1a07")
            @RequestParam UUID commandeId) {
        String clientId = jeton.getClaimAsString("azp");
        log.info("[adapter.in] GET /api/slice-service/v1/service?commandeId={} clientId={}", commandeId, clientId);
        return activationService.servicesDeLaCommande(clientId, commandeId).stream()
                .map(ServiceResponse::depuis)
                .toList();
    }
}
