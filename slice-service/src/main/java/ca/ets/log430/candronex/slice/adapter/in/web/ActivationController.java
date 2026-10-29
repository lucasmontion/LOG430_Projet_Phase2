package ca.ets.log430.candronex.slice.adapter.in.web;

import ca.ets.log430.candronex.slice.application.ActivationService;
import ca.ets.log430.candronex.slice.application.DemanderActivationCommand;
import ca.ets.log430.candronex.slice.application.ResultatDemande;
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
@RequestMapping("/api/slice-service/v1")
public class ActivationController {
    private static final Logger log = LoggerFactory.getLogger(ActivationController.class);

    private final ActivationService activationService;

    public ActivationController(ActivationService activationService) {
        this.activationService = activationService;
    }

    /** 202 : demande acceptée, activation en arrière-plan. 200 : rejeu, service existant. */
    @PostMapping("/activation")
    public ResponseEntity<ServiceResponse> demander(
            @AuthenticationPrincipal Jwt jeton,
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

    @GetMapping("/service/{serviceId}")
    public ServiceResponse consulter(@AuthenticationPrincipal Jwt jeton, @PathVariable String serviceId) {
        String clientId = jeton.getClaimAsString("azp");
        log.info("[adapter.in] GET /api/slice-service/v1/service/{} clientId={}", serviceId, clientId);
        return ServiceResponse.depuis(activationService.consulter(clientId, serviceId));
    }

    @GetMapping("/service")
    public List<ServiceResponse> servicesDeLaCommande(@AuthenticationPrincipal Jwt jeton,
                                                      @RequestParam UUID commandeId) {
        String clientId = jeton.getClaimAsString("azp");
        log.info("[adapter.in] GET /api/slice-service/v1/service?commandeId={} clientId={}", commandeId, clientId);
        return activationService.servicesDeLaCommande(clientId, commandeId).stream()
                .map(ServiceResponse::depuis)
                .toList();
    }
}
