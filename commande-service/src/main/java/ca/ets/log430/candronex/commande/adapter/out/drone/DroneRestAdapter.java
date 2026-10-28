package ca.ets.log430.candronex.commande.adapter.out.drone;

import ca.ets.log430.candronex.commande.domain.DroneId;
import ca.ets.log430.candronex.commande.domain.DroneVerifie;
import ca.ets.log430.candronex.commande.domain.VerificationDronePort;
import ca.ets.log430.candronex.commun.adapter.in.web.CorrelationIdFilter;
import ca.ets.log430.candronex.commun.domaine.ClientId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Optional;

/**
 * Adaptateur de sortie : implémente VerificationDronePort par un appel REST à drone-service.
 * Transmet le JWT du client (drone-service le revalide et filtre par azp) et l'identifiant de corrélation.
 */
@Component
class DroneRestAdapter implements VerificationDronePort {
    private static final Logger log = LoggerFactory.getLogger(DroneRestAdapter.class);

    private final RestClient client;

    DroneRestAdapter(RestClient.Builder builder, @Value("${candronex.drone-service.url}") String url) {
        // Délais d'attente : un drone-service lent ne doit pas bloquer la commande indéfiniment
        SimpleClientHttpRequestFactory delais = new SimpleClientHttpRequestFactory();
        delais.setConnectTimeout(Duration.ofSeconds(2));
        delais.setReadTimeout(Duration.ofSeconds(3));
        this.client = builder.baseUrl(url).requestFactory(delais).build();
    }

    @Override
    public Optional<DroneVerifie> verifier(ClientId clientId, DroneId droneId) {
        log.info("[adapter.out] GET drone-service droneId={}", droneId.valeur());
        try {
            DroneDto drone = client.get()
                    .uri("/api/drone-service/v1/drone/{droneId}", droneId.valeur())
                    .header(HttpHeaders.AUTHORIZATION, jetonCourant())
                    .header(CorrelationIdFilter.EN_TETE, MDC.get(CorrelationIdFilter.CLE_MDC))
                    .retrieve()
                    .body(DroneDto.class);
            return Optional.of(new DroneVerifie(new DroneId(drone.droneId()), drone.statut()));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();   // inexistant ou drone d'un autre client : même réponse
        } catch (RestClientException e) {
            log.warn("[adapter.out] drone-service indisponible : {}", e.getMessage());
            throw new DroneServiceIndisponibleException();
        }
    }

    /** Le jeton de la requête en cours, tel que reçu : drone-service fera sa propre validation. */
    private String jetonCourant() {
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return "Bearer " + auth.getToken().getTokenValue();
    }

    /** DTO dédié : seuls les champs dont la commande a besoin (les autres sont ignorés). */
    record DroneDto(String droneId, String statut) {}
}