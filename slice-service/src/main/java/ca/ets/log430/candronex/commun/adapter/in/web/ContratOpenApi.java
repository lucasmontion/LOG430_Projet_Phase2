package ca.ets.log430.candronex.commun.adapter.in.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;
import java.util.Map;

/**
 * Éléments communs des contrats OpenAPI : sécurité Bearer (JWT Keycloak) et réponses d'erreur
 * au format Problem Details (RFC 9457), tel que produit par GestionnaireErreurs et RefusNonAuthentifie.
 * Les contrôleurs y renvoient avec @ApiResponse(ref = REPONSES + "NonAuthentifie"), par exemple ;
 * chaque service ajoute ses propres erreurs (404, 409, 422...) avec ses codes, par erreur(...).
 * Exemples : swagger-core lit un exemple comme du JSON (« 20 Mbps » deviendrait 20, un IMSI deviendrait un nombre) ;
 * on écrit donc ces exemples-là entre guillemets JSON, ex. example = "\"20 Mbps\"".
 */
public final class ContratOpenApi {
    public static final String REPONSES = "#/components/responses/";
    public static final String SECURITE = "jetonBearer";
    private static final String PROBLEM_JSON = "application/problem+json";

    private ContratOpenApi() {}

    /** Contrat de base d'un service : identité, serveur interne, sécurité et erreurs communes (400, 401, 500). */
    public static OpenAPI base(String titre, String description, String urlInterne) {
        Components composants = new Components()
                .addSecuritySchemes(SECURITE, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                        .description("Jeton d'accès émis par Keycloak (realm candronex, audience candronex-api). "
                                + "Le client est identifié par la claim azp du jeton vérifié."))
                .addSchemas("ProblemDetails", problemDetails());
        erreur(composants, "RequeteInvalide", 400, "Requête invalide : corps mal formé, champ ou en-tête "
                + "obligatoire manquant, ou format refusé par le domaine", "REQUETE_INVALIDE",
                "Requête mal formée, ou champ, paramètre ou en-tête obligatoire manquant ou invalide");
        erreur(composants, "NonAuthentifie", 401, "Jeton Bearer absent, invalide ou expiré "
                + "(en-tête WWW-Authenticate: Bearer)", "NON_AUTHENTIFIE", "Jeton Bearer absent, invalide ou expiré");
        erreur(composants, "ErreurInterne", 500, "Erreur inattendue ; aucun détail technique n'est exposé",
                "ERREUR_INTERNE", "Erreur interne. Communiquez le correlationId au support.");

        return new OpenAPI()
                .info(new Info().title(titre).version("v1").description(description))
                .servers(List.of(new Server().url(urlInterne)
                        .description("Réseau Docker interne seulement : de l'extérieur, passer par la passerelle")))
                .components(composants)
                .addSecurityItem(new SecurityRequirement().addList(SECURITE));
    }

    /** Ajoute une réponse d'erreur réutilisable, avec un exemple. */
    public static void erreur(Components composants, String nom, int statut, String description,
                              String code, String detail) {
        Map<String, Object> exemple = Map.of(
                "type", "urn:candronex:erreur:" + code,
                "title", titre(statut),
                "status", statut,
                "detail", detail,
                "code", code,
                "correlationId", "3f2b8c1e-5d0a-4e7b-9c61-2a4f8e0d7b13");
        composants.addResponses(nom, new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(PROBLEM_JSON, new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ProblemDetails"))
                        .example(exemple))));
    }

    private static Schema<?> problemDetails() {
        return new ObjectSchema()
                .description("Format d'erreur uniforme des 3 services (RFC 9457, application/problem+json)")
                .addProperty("type", new StringSchema().format("uri")
                        .description("Identifiant du type d'erreur").example("urn:candronex:erreur:DRONE_INTROUVABLE"))
                .addProperty("title", new StringSchema().description("Libellé du statut HTTP").example("Not Found"))
                .addProperty("status", new IntegerSchema().description("Statut HTTP").example(404))
                .addProperty("detail", new StringSchema().description("Explication lisible par un humain")
                        .example("Drone DRONE-600 introuvable"))
                .addProperty("instance", new StringSchema().format("uri").description("Chemin de la requête")
                        .example("/api/drone-service/v1/drone/DRONE-600"))
                .addProperty("code", new StringSchema()
                        .description("Code stable, destiné aux programmes clients").example("DRONE_INTROUVABLE"))
                .addProperty("correlationId", new StringSchema()
                        .description("Identifiant de la requête dans les journaux (en-tête X-Correlation-Id)")
                        .example("3f2b8c1e-5d0a-4e7b-9c61-2a4f8e0d7b13"))
                .required(List.of("type", "title", "status", "code"));
    }

    private static String titre(int statut) {
        return org.springframework.http.HttpStatus.valueOf(statut).getReasonPhrase();
    }
}
