package ca.ets.log430.candronex.commande.adapter.out.slice;

import ca.ets.log430.candronex.commande.domain.*;
import ca.ets.log430.candronex.commun.adapter.in.web.CorrelationIdFilter;
import ca.ets.log430.candronex.commun.domaine.ClientId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptateur de sortie : implémente ActivationPort par les routes internes de slice-service.
 * Couche anticorruption : traduit l'état d'un service de l'inventaire en état d'élément de commande (§6.2).
 * Une indisponibilité de slice-service ne fait jamais échouer la commande : l'élément reste RECU.
 */
@Component
class SliceRestAdapter implements ActivationPort {
    private static final Logger log = LoggerFactory.getLogger(SliceRestAdapter.class);

    private final RestClient client;

    SliceRestAdapter(RestClient.Builder builder, @Value("${candronex.slice-service.url}") String url) {
        SimpleClientHttpRequestFactory delais = new SimpleClientHttpRequestFactory();
        delais.setConnectTimeout(Duration.ofSeconds(2));
        delais.setReadTimeout(Duration.ofSeconds(3));
        this.client = builder.baseUrl(url).requestFactory(delais).build();
    }

    @Override
    public Optional<EtatElement> demanderActivation(ClientId clientId, CommandeId commandeId,
                                                    ElementCommande element, Instant acceptedAt) {
        CaracteristiquesFigees c = element.caracteristiques();
        DemandeDto demande = new DemandeDto(commandeId.valeur(), element.id().valeur(), element.droneId().valeur(),
                element.typeService().valeur(), acceptedAt,
                new CaracteristiquesDto(c.sst(), c.sd(), c.dnn(), c.fiveQi(), c.arp(), c.ambrMontant(), c.ambrDescendant()));
        log.info("[adapter.out] POST slice-service activation elementId={}", element.id().valeur());
        try {
            ServiceDto service = client.post()
                    .uri("/api/slice-service/v1/activation")
                    .header(HttpHeaders.AUTHORIZATION, jetonCourant())
                    .header(CorrelationIdFilter.EN_TETE, MDC.get(CorrelationIdFilter.CLE_MDC))
                    .header(CorrelationIdFilter.EN_TETE_COMMANDE, commandeId.valeur().toString())
                    .body(demande)
                    .retrieve()
                    .body(ServiceDto.class);
            return Optional.of(versEtatElement(service.etat()));
        } catch (RestClientException e) {
            log.warn("[adapter.out] slice-service indisponible (activation) : {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Map<ElementId, EtatElement>> etatsDesElements(ClientId clientId, CommandeId commandeId) {
        try {
            List<ServiceDto> services = client.get()
                    .uri("/api/slice-service/v1/service?commandeId={id}", commandeId.valeur())
                    .header(HttpHeaders.AUTHORIZATION, jetonCourant())
                    .header(CorrelationIdFilter.EN_TETE, MDC.get(CorrelationIdFilter.CLE_MDC))
                    .header(CorrelationIdFilter.EN_TETE_COMMANDE, commandeId.valeur().toString())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return Optional.of(services.stream().collect(Collectors.toMap(
                    s -> new ElementId(s.elementId()), s -> versEtatElement(s.etat()))));
        } catch (RestClientException e) {
            log.warn("[adapter.out] slice-service indisponible (lecture des états) : {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** Correspondance du §6.2 : état du service dans l'inventaire -> état de l'élément de commande. */
    static EtatElement versEtatElement(String etatService) {
        return switch (etatService) {
            case "EN_ATTENTE", "EN_ACTIVATION" -> EtatElement.EN_COURS;
            case "ACTIF" -> EtatElement.ACTIF;
            case "ECHEC", "ECHEC_INDETERMINE" -> EtatElement.ECHEC;
            default -> throw new IllegalStateException("État de service inconnu : " + etatService);
        };
    }

    private String jetonCourant() {
        JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return "Bearer " + auth.getToken().getTokenValue();
    }

    /** DTO dédiés au contrat de slice-service (jamais les classes internes de ce service). */
    record DemandeDto(UUID commandeId, UUID elementId, String droneId, String typeService, Instant acceptedAt,
                      CaracteristiquesDto caracteristiques) {}

    record CaracteristiquesDto(int sst, String sd, String dnn, int fiveQi, int arp,
                               String ambrMontant, String ambrDescendant) {}

    record ServiceDto(UUID serviceId, UUID elementId, String etat) {}
}
