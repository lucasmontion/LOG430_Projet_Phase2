package ca.ets.log430.candronex.commande.adapter.in.web;

import ca.ets.log430.candronex.commande.application.CommandeService;
import ca.ets.log430.candronex.commande.application.PasserCommandeCommand;
import ca.ets.log430.candronex.commande.application.ResultatCommande;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Adaptateur d'entrée REST de UC-04.
 * Le client est identifié par la claim azp du JWT, vérifié par Spring Security (SecuriteConfig).
 */
@RestController
@RequestMapping(CommandeController.CHEMIN)
public class CommandeController {
    static final String CHEMIN = "/api/commande-service/v1/commande";
    private static final Logger log = LoggerFactory.getLogger(CommandeController.class);

    private final CommandeService commandeService;

    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    @PostMapping
    public ResponseEntity<CommandeResponse> passer(
            @AuthenticationPrincipal Jwt jeton,
            @RequestHeader(value = "Idempotency-Key", required = false) String cleIdempotence,
            @Valid @RequestBody PasserCommandeRequest requete) {
        String clientId = jeton.getClaimAsString("azp");   // identité du client : claim azp du jeton VÉRIFIÉ
        log.info("[adapter.in] POST {} clientId={} elements={}", CHEMIN, clientId, requete.elements().size());

        ResultatCommande resultat = commandeService.passer(new PasserCommandeCommand(
                clientId, cleIdempotence,
                requete.elements().stream()
                        .map(e -> new PasserCommandeCommand.Element(e.droneId(), e.typeService()))
                        .toList()));

        CommandeResponse corps = CommandeResponse.depuis(resultat.commande());
        if (!resultat.nouvelle()) {
            log.info("[adapter.in] POST {} -> 200 (rejeu) commandeId={}", CHEMIN, corps.commandeId());
            return ResponseEntity.ok(corps);
        }
        log.info("[adapter.in] POST {} -> 201 commandeId={}", CHEMIN, corps.commandeId());
        return ResponseEntity.created(URI.create(CHEMIN + "/" + corps.commandeId())).body(corps);
    }

    @GetMapping("/{commandeId}")
    public CommandeResponse consulter(
            @AuthenticationPrincipal Jwt jeton,
            @PathVariable String commandeId) {
        String clientId = jeton.getClaimAsString("azp");
        log.info("[adapter.in] GET {}/{} clientId={}", CHEMIN, commandeId, clientId);
        return CommandeResponse.depuis(commandeService.consulter(clientId, commandeId));
    }
}
