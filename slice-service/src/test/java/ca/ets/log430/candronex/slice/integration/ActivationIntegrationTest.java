package ca.ets.log430.candronex.slice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Tests d'intégration de UC-06 exigés par l'énoncé, réseau simulé sans échec (MOCK_TAUX_ECHEC = 0). */
class ActivationIntegrationTest extends IntegrationSliceBase {

    @Test
    void sansJetonLaDemandeEstRefuseeEtRienNEstCree() throws Exception {
        UUID commandeId = UUID.randomUUID();

        demanderActivation(null, commandeId, UUID.randomUUID())
                .andExpect(status().isUnauthorized());

        assertThat(nombreDeServices(commandeId)).isZero();
    }

    @Test
    void unJetonInvalideEstRefuseEtRienNEstCree() throws Exception {
        UUID commandeId = UUID.randomUUID();

        demanderActivation(INVALIDE, commandeId, UUID.randomUUID())
                .andExpect(status().isUnauthorized());

        assertThat(nombreDeServices(commandeId)).isZero();
    }

    @Test
    void uneActivationReussieMeneLeServiceAActif() throws Exception {
        String serviceId = champ(demanderActivation(INSPECTRA, UUID.randomUUID(), UUID.randomUUID())
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.etat").value("EN_ATTENTE")), "$.serviceId");

        attendreEtat(serviceId, "ACTIF");

        consulter(INSPECTRA, serviceId)
                .andExpect(jsonPath("$.activatedAt").isNotEmpty())
                .andExpect(jsonPath("$.imsi").doesNotExist());   // l'IMSI ne sort jamais du service
    }

    @Test
    void unRejeuRenvoieLeMemeServiceSansDoublon() throws Exception {
        UUID commandeId = UUID.randomUUID();
        UUID elementId = UUID.randomUUID();

        String premier = champ(demanderActivation(INSPECTRA, commandeId, elementId)
                .andExpect(status().isAccepted()), "$.serviceId");
        String rejeu = champ(demanderActivation(INSPECTRA, commandeId, elementId)
                .andExpect(status().isOk()), "$.serviceId");

        assertThat(rejeu).isEqualTo(premier);
        assertThat(nombreDeServices(commandeId)).isEqualTo(1);
    }

    @Test
    void unAutreClientNeVoitPasLeService() throws Exception {
        UUID commandeId = UUID.randomUUID();
        String serviceId = champ(demanderActivation(INSPECTRA, commandeId, UUID.randomUUID()), "$.serviceId");

        consulter(AEROTECH, serviceId)
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/slice-service/v1/service").param("commandeId", commandeId.toString())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + AEROTECH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void unCommandeIdQuiNEstPasUnUuidDonne400EtNon500() throws Exception {
        mvc.perform(get("/api/slice-service/v1/service").param("commandeId", "pas-un-uuid")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + INSPECTRA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUETE_INVALIDE"));
    }
}
