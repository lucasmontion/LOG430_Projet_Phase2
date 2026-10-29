package ca.ets.log430.candronex.slice.adapter.out.drone;

import ca.ets.log430.candronex.commun.adapter.in.web.CorrelationIdFilter;
import ca.ets.log430.candronex.commun.domaine.ClientId;
import ca.ets.log430.candronex.slice.domain.DroneId;
import ca.ets.log430.candronex.slice.domain.IdentiteReseau;
import ca.ets.log430.candronex.slice.domain.IdentiteReseauIndisponibleException;
import ca.ets.log430.candronex.slice.domain.IdentiteReseauPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Optional;

/**
 * Adaptateur de sortie : implémente IdentiteReseauPort par la route interne identite-reseau de drone-service.
 * Transmet le JWT du client (drone-service le revalide et filtre par azp) et l'identifiant de corrélation.
 * L'activation s'exécute en arrière-plan : le contexte de sécurité y est propagé par l'exécuteur (voir configuration).
 */
@Component
class IdentiteReseauRestAdapter implements IdentiteReseauPort {
    private static final Logger log = LoggerFactory.getLogger(IdentiteReseauRestAdapter.class);

    private final RestClient client;

    IdentiteReseauRestAdapter(RestClient.Builder builder, @Value("${candronex.drone-service.url}") String url) {
        SimpleClientHttpRequestFactory delais = new SimpleClientHttpRequestFactory();
        delais.setConnectTimeout(Duration.ofSeconds(2));
        delais.setReadTimeout(Duration.ofSeconds(3));
        this.client = builder.baseUrl(url).requestFactory(delais).build();
    }

    @Override
    public Optional<IdentiteReseau> resoudre(ClientId clientId, DroneId droneId) {
        log.info("[adapter.out] GET drone-service identite-reseau droneId={}", droneId.valeur());
        try {
            IdentiteDto identite = client.get()
                    .uri("/api/drone-service/v1/drone/{droneId}/identite-reseau", droneId.valeur())
                    .header(HttpHeaders.AUTHORIZATION, jetonCourant())
                    .header(CorrelationIdFilter.EN_TETE, MDC.get(CorrelationIdFilter.CLE_MDC))
                    .retrieve()
                    .body(IdentiteDto.class);
            if (!"ACTIF".equals(identite.statut())) {
                log.info("[adapter.out] drone non actif droneId={} statut={}", droneId.valeur(), identite.statut());
                return Optional.empty();
            }
            return Optional.of(new IdentiteReseau(identite.imsi()));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();   // inexistant ou drone d'un autre client
        } catch (RestClientException e) {
            log.warn("[adapter.out] drone-service injoignable : {}", e.getMessage());
            throw new IdentiteReseauIndisponibleException("drone-service injoignable");
        }
    }

    /** Le jeton du client qui a demandé l'activation, tel que reçu : drone-service fera sa propre validation. */
    private String jetonCourant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwt)) {
            throw new IdentiteReseauIndisponibleException("aucun jeton à transmettre à drone-service");
        }
        return "Bearer " + jwt.getToken().getTokenValue();
    }

    /** DTO dédié : seuls les champs utiles à l'activation. */
    record IdentiteDto(String droneId, String imsi, String statut) {}
}
