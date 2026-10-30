package ca.ets.log430.candronex.slice.integration;

import ca.ets.log430.candronex.slice.domain.IdentiteReseau;
import ca.ets.log430.candronex.slice.domain.IdentiteReseauPort;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base des tests d'intégration de slice-service : le service démarre en entier
 * (sécurité, contrôleurs, Flyway, JPA sur H2) et il est appelé en HTTP par MockMvc.
 * Seul ce qui sort du service est remplacé : Keycloak (décodeur de jeton) et drone-service (IdentiteReseauPort).
 * Le réseau simulé est le vrai MockOpen5GSAdapter, avec une latence courte.
 */
@SpringBootTest(properties = {
        "candronex.mock.latence-min-ms=0",
        "candronex.mock.latence-max-ms=20"})
@AutoConfigureMockMvc
abstract class IntegrationSliceBase {
    static final String INSPECTRA = "jeton-inspectra";
    static final String AEROTECH = "jeton-aerotech";
    static final String INVALIDE = "jeton-invalide";

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @MockitoBean JwtDecoder decodeurJwt;
    @MockitoBean IdentiteReseauPort identiteReseau;

    @BeforeEach
    void preparerLesDoublures() {
        when(decodeurJwt.decode(INSPECTRA)).thenReturn(jeton("inspectra-demo"));
        when(decodeurJwt.decode(AEROTECH)).thenReturn(jeton("aerotech-demo"));
        when(decodeurJwt.decode(INVALIDE)).thenThrow(new BadJwtException("Signature invalide"));
        when(identiteReseau.resoudre(any(), any()))
                .thenReturn(Optional.of(new IdentiteReseau("999700000000600")));
    }

    /** Ce que Keycloak aurait émis pour ce client, une fois la signature vérifiée. */
    static Jwt jeton(String client) {
        Instant maintenant = Instant.now();
        return Jwt.withTokenValue("jeton-" + client)
                .header("alg", "RS256")
                .subject(client)
                .claim("azp", client)
                .issuedAt(maintenant)
                .expiresAt(maintenant.plusSeconds(300))
                .build();
    }

    /** POST /activation, comme le ferait commande-service ; sans jeton si jeton == null. */
    ResultActions demanderActivation(String jeton, UUID commandeId, UUID elementId) throws Exception {
        MockHttpServletRequestBuilder requete = post("/api/slice-service/v1/activation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corps(commandeId, elementId));
        if (jeton != null) {
            requete.header(HttpHeaders.AUTHORIZATION, "Bearer " + jeton);
        }
        return mvc.perform(requete);
    }

    ResultActions consulter(String jeton, String serviceId) throws Exception {
        return mvc.perform(get("/api/slice-service/v1/service/{id}", serviceId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jeton));
    }

    static String corps(UUID commandeId, UUID elementId) {
        return """
                {"commandeId": "%s", "elementId": "%s", "droneId": "DRONE-600", "typeService": "C2",
                 "acceptedAt": "%s",
                 "caracteristiques": {"sst": 2, "sd": "000001", "dnn": "c2", "fiveQi": 7, "arp": 2,
                                      "ambrMontant": "20 Mbps", "ambrDescendant": "20 Mbps"}}
                """.formatted(commandeId, elementId, Instant.now());
    }

    static String champ(ResultActions resultat, String chemin) throws Exception {
        return JsonPath.read(resultat.andReturn().getResponse().getContentAsString(), chemin);
    }

    /** L'activation se fait en arrière-plan : on relit l'état jusqu'à obtenir celui attendu (5 s au plus). */
    void attendreEtat(String serviceId, String attendu) throws Exception {
        long limite = System.currentTimeMillis() + 5000;
        String etat = null;
        while (System.currentTimeMillis() < limite) {
            etat = champ(consulter(INSPECTRA, serviceId), "$.etat");
            if (attendu.equals(etat)) {
                return;
            }
            Thread.sleep(50);
        }
        throw new AssertionError("État attendu " + attendu + ", mais le service est resté " + etat);
    }

    /** Lu directement en base : prouve qu'aucun doublon n'a été créé. */
    int nombreDeServices(UUID commandeId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM service WHERE commande_id = ?", Integer.class, commandeId);
    }
}