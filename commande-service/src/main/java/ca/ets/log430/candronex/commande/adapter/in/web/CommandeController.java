package ca.ets.log430.candronex.commande.adapter.in.web;

import ca.ets.log430.candronex.commande.application.CommandeService;
import ca.ets.log430.candronex.commun.adapter.in.web.ContratOpenApi;
import ca.ets.log430.candronex.commande.application.PasserCommandeCommand;
import ca.ets.log430.candronex.commande.application.ResultatCommande;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Commandes", description = "UC-04 : commander les services réseau des drones du client authentifié")
@RequestMapping(CommandeController.CHEMIN)
public class CommandeController {
    static final String CHEMIN = "/api/commande-service/v1/commande";
    private static final Logger log = LoggerFactory.getLogger(CommandeController.class);

    private final CommandeService commandeService;

    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    @Operation(summary = "Commander des services pour ses drones (UC-04)",
            description = "Chaque élément associe un drone du client à un service du catalogue (C2, IMAGERIE). "
                    + "Les caractéristiques réseau sont copiées du catalogue, jamais fournies par le client. "
                    + "Une fois la commande enregistrée, l'activation de chaque élément est demandée à slice-service "
                    + "(UC-06) ; si slice-service est indisponible, la commande est tout de même créée (état RECUE).\n\n"
                    + "Idempotence : renvoyer la même requête avec la même Idempotency-Key renvoie la commande "
                    + "existante (200), sans doublon.\n\n"
                    + "Codes d'erreur : 400 REQUETE_INVALIDE, CLE_IDEMPOTENCE_INVALIDE, COMMANDE_VIDE, DRONE_ID_INVALIDE, "
                    + "TYPE_SERVICE_INVALIDE ; 404 DRONE_INTROUVABLE ; 409 CLE_IDEMPOTENCE_REUTILISEE ; "
                    + "422 DRONE_NON_ELIGIBLE, SERVICE_INCONNU, ELEMENT_EN_DOUBLE ; 503 DRONE_SERVICE_INDISPONIBLE.")
    @ApiResponse(responseCode = "201", description = "Commande créée ; activation demandée (état EN_COURS) "
            + "ou en attente (RECUE)", headers = @Header(name = "Location", description = "URL de la commande créée"))
    @ApiResponse(responseCode = "200", description = "Rejeu : même clé et même contenu, la commande existante")
    @ApiResponse(responseCode = "400", ref = ContratOpenApi.REPONSES + "RequeteInvalide")
    @ApiResponse(responseCode = "401", ref = ContratOpenApi.REPONSES + "NonAuthentifie")
    @ApiResponse(responseCode = "404", ref = ContratOpenApi.REPONSES + "Introuvable")
    @ApiResponse(responseCode = "409", ref = ContratOpenApi.REPONSES + "CleReutilisee")
    @ApiResponse(responseCode = "422", ref = ContratOpenApi.REPONSES + "RegleMetier")
    @ApiResponse(responseCode = "500", ref = ContratOpenApi.REPONSES + "ErreurInterne")
    @ApiResponse(responseCode = "503", ref = ContratOpenApi.REPONSES + "DependanceIndisponible")
    @PostMapping
    public ResponseEntity<CommandeResponse> passer(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jeton,
            // required = false : l'absence est refusée par le domaine (400 CLE_IDEMPOTENCE_INVALIDE), pas par Spring
            @Parameter(required = true, description = "Clé choisie par le client (UUID conseillé, 100 caractères max.) : "
                    + "deux requêtes avec la même clé sont la même commande",
                    schema = @Schema(type = "string", maxLength = 100), example = "8b0e6c52-3f1a-4d9e-b7c4-2a5d9e1f0c63")
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

    @Operation(summary = "Consulter une commande du client (UC-04)",
            description = "L'état de chaque élément est actualisé depuis l'inventaire de slice-service ; s'il est "
                    + "indisponible, le dernier état connu est renvoyé. La commande d'un autre client donne 404.\n\n"
                    + "Codes d'erreur : 400 COMMANDE_ID_INVALIDE ; 404 COMMANDE_INTROUVABLE.")
    @ApiResponse(responseCode = "200", description = "La commande et l'état de ses éléments")
    @ApiResponse(responseCode = "400", ref = ContratOpenApi.REPONSES + "RequeteInvalide")
    @ApiResponse(responseCode = "401", ref = ContratOpenApi.REPONSES + "NonAuthentifie")
    @ApiResponse(responseCode = "404", ref = ContratOpenApi.REPONSES + "Introuvable")
    @ApiResponse(responseCode = "500", ref = ContratOpenApi.REPONSES + "ErreurInterne")
    @GetMapping("/{commandeId}")
    public CommandeResponse consulter(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jeton,
            @Parameter(description = "Identifiant de la commande (UUID)", example = "6f1c2a9e-0b7d-4c3e-9a51-8d2e4f6b1a07")
            @PathVariable String commandeId) {
        String clientId = jeton.getClaimAsString("azp");
        log.info("[adapter.in] GET {}/{} clientId={}", CHEMIN, commandeId, clientId);
        return CommandeResponse.depuis(commandeService.consulter(clientId, commandeId));
    }
}
