package ca.ets.log430.candronex.commande.integration;

import ca.ets.log430.candronex.commande.domain.ActivationPort;
import ca.ets.log430.candronex.commande.domain.DroneId;
import ca.ets.log430.candronex.commande.domain.DroneVerifie;
import ca.ets.log430.candronex.commande.domain.EtatElement;
import ca.ets.log430.candronex.commande.domain.VerificationDronePort;
import com.jayway.jsonpath.JsonPath;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de UC-04 : commande-service démarre en entier (sécurité, contrôleur, Flyway, JPA sur H2)
 * et il est appelé en HTTP. Seul ce qui sort du service est remplacé :
 * Keycloak (décodeur de jeton), drone-service (VerificationDronePort) et slice-service (ActivationPort).
 */
@SpringBootTest
@AutoConfigureMockMvc
class CommandeIntegrationTest {
    static final String INSPECTRA = "jeton-inspectra";
    static final String AEROTECH = "jeton-aerotech";
    static final String INVALIDE = "jeton-invalide";

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @MockitoBean JwtDecoder decodeurJwt;
    @MockitoBean VerificationDronePort verificationDrone;
    @MockitoBean ActivationPort activation;

    @BeforeEach
    void preparerLesDoublures() {
        when(decodeurJwt.decode(INSPECTRA)).thenReturn(jeton("inspectra-demo"));
        when(decodeurJwt.decode(AEROTECH)).thenReturn(jeton("aerotech-demo"));
        when(decodeurJwt.decode(INVALIDE)).thenThrow(new BadJwtException("Signature invalide"));
        when(verificationDrone.verifier(any(), any()))
                .thenReturn(Optional.of(new DroneVerifie(new DroneId("DRONE-600"), "ACTIF")));
        when(activation.demanderActivation(any(), any(), any(), any()))
                .thenReturn(Optional.of(EtatElement.EN_COURS));
        when(activation.etatsDesElements(any(), any())).thenReturn(Optional.of(Map.of()));
    }

    @Test
    void sansJetonLaCommandeEstRefuseeEtRienNEstCree() throws Exception {
        String cle = UUID.randomUUID().toString();

        passerCommande(null, cle).andExpect(status().isUnauthorized());

        assertThat(nombreDeCommandes(cle)).isZero();
    }

    @Test
    void unJetonInvalideEstRefuseEtRienNEstCree() throws Exception {
        String cle = UUID.randomUUID().toString();

        passerCommande(INVALIDE, cle).andExpect(status().isUnauthorized());

        assertThat(nombreDeCommandes(cle)).isZero();
    }

    @Test
    void uneCommandeValideEstCreeEtSonActivationDemandee() throws Exception {
        passerCommande(INSPECTRA, UUID.randomUUID().toString())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.etat").value("EN_COURS"))
                .andExpect(jsonPath("$.elements[0].caracteristiques.dnn").value("c2"));
    }

    @Test
    void unRejeuAvecLaMemeCleRenvoieLaMemeCommandeSansDoublon() throws Exception {
        String cle = UUID.randomUUID().toString();

        String premiere = champ(passerCommande(INSPECTRA, cle).andExpect(status().isCreated()), "$.commandeId");
        String rejeu = champ(passerCommande(INSPECTRA, cle).andExpect(status().isOk()), "$.commandeId");

        assertThat(rejeu).isEqualTo(premiere);
        assertThat(nombreDeCommandes(cle)).isEqualTo(1);
    }

    @Test
    void unAutreClientNeVoitPasLaCommande() throws Exception {
        String commandeId = champ(passerCommande(INSPECTRA, UUID.randomUUID().toString()), "$.commandeId");

        mvc.perform(get("/api/commande-service/v1/commande/{id}", commandeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + AEROTECH))
                .andExpect(status().isNotFound());
    }

    @Test
    void unDroneInconnuOuDUnAutreClientEstRefuseEtRienNEstCree() throws Exception {
        when(verificationDrone.verifier(any(), any())).thenReturn(Optional.empty());
        String cle = UUID.randomUUID().toString();

        passerCommande(INSPECTRA, cle).andExpect(status().isNotFound());

        assertThat(nombreDeCommandes(cle)).isZero();
    }

    @Test
    void sliceServiceIndisponibleNEmpechePasLaCommande() throws Exception {
        when(activation.demanderActivation(any(), any(), any(), any())).thenReturn(Optional.empty());

        passerCommande(INSPECTRA, UUID.randomUUID().toString())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.etat").value("RECUE"))
                .andExpect(jsonPath("$.elements[0].etat").value("RECU"));
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

    /** POST /commande avec une clé d'idempotence ; sans jeton si jeton == null. */
    ResultActions passerCommande(String jeton, String cle) throws Exception {
        MockHttpServletRequestBuilder requete = post("/api/commande-service/v1/commande")
                .header("Idempotency-Key", cle)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"elements": [{"droneId": "DRONE-600", "typeService": "C2"}]}
                        """);
        if (jeton != null) {
            requete.header(HttpHeaders.AUTHORIZATION, "Bearer " + jeton);
        }
        return mvc.perform(requete);
    }

    static String champ(ResultActions resultat, String chemin) throws Exception {
        return JsonPath.read(resultat.andReturn().getResponse().getContentAsString(), chemin);
    }

    /** Lu directement en base : prouve qu'aucun doublon n'a été créé. */
    int nombreDeCommandes(String cle) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM commande WHERE cle_idempotence = ?", Integer.class, cle);
    }
}