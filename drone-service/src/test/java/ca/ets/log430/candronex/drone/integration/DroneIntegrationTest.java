package ca.ets.log430.candronex.drone.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de UC-02 : drone-service démarre en entier (sécurité, contrôleur, Flyway, JPA sur H2)
 * et il est appelé en HTTP. Seul Keycloak est remplacé (décodeur de jeton) : drone-service n'appelle personne.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DroneIntegrationTest {
    static final String INSPECTRA = "jeton-inspectra";
    static final String AEROTECH = "jeton-aerotech";
    static final String INVALIDE = "jeton-invalide";

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @MockitoBean JwtDecoder decodeurJwt;

    @BeforeEach
    void preparerLesDoublures() {
        when(decodeurJwt.decode(INSPECTRA)).thenReturn(jeton("inspectra-demo"));
        when(decodeurJwt.decode(AEROTECH)).thenReturn(jeton("aerotech-demo"));
        when(decodeurJwt.decode(INVALIDE)).thenThrow(new BadJwtException("Signature invalide"));
    }

    @Test
    void sansJetonLEnregistrementEstRefuseEtRienNEstCree() throws Exception {
        String droneId = nouveauDroneId();

        enregistrer(null, droneId).andExpect(status().isUnauthorized());

        assertThat(nombreDeDrones(droneId)).isZero();
    }

    @Test
    void unJetonInvalideEstRefuseEtRienNEstCree() throws Exception {
        String droneId = nouveauDroneId();

        enregistrer(INVALIDE, droneId).andExpect(status().isUnauthorized());

        assertThat(nombreDeDrones(droneId)).isZero();
    }

    @Test
    void unDroneEnregistreEstConsultableParSonClientSansIdentiteReseau() throws Exception {
        String droneId = nouveauDroneId();

        enregistrer(INSPECTRA, droneId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("ACTIF"));

        consulter(INSPECTRA, droneId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeSim").value("ESIM"))
                .andExpect(jsonPath("$.imsi").doesNotExist())     // l'IMSI ne sort que par la route interne
                .andExpect(jsonPath("$.iccid").doesNotExist());
    }

    @Test
    void unAutreClientNeVoitPasLeDrone() throws Exception {
        String droneId = nouveauDroneId();
        enregistrer(INSPECTRA, droneId).andExpect(status().isCreated());

        consulter(AEROTECH, droneId).andExpect(status().isNotFound());
        mvc.perform(get("/api/drone-service/v1/drone/{id}/identite-reseau", droneId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + AEROTECH))
                .andExpect(status().isNotFound());
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

    /** POST /drone ; l'IMSI et l'ICCID sont tirés au hasard, car ils sont uniques dans tout le système. */
    ResultActions enregistrer(String jeton, String droneId) throws Exception {
        ThreadLocalRandom hasard = ThreadLocalRandom.current();
        String imsi = "99970" + String.format("%010d", hasard.nextLong(10_000_000_000L));
        String iccid = "899970" + String.format("%013d", hasard.nextLong(10_000_000_000_000L));
        MockHttpServletRequestBuilder requete = post("/api/drone-service/v1/drone")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"droneId": "%s", "imsi": "%s", "iccid": "%s", "typeSim": "ESIM"}
                        """.formatted(droneId, imsi, iccid));
        if (jeton != null) {
            requete.header(HttpHeaders.AUTHORIZATION, "Bearer " + jeton);
        }
        return mvc.perform(requete);
    }

    ResultActions consulter(String jeton, String droneId) throws Exception {
        return mvc.perform(get("/api/drone-service/v1/drone/{id}", droneId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jeton));
    }

    static String nouveauDroneId() {
        return "DRONE-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /** Lu directement en base : prouve que rien n'a été créé. */
    int nombreDeDrones(String droneId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM drone WHERE drone_id = ?", Integer.class, droneId);
    }
}