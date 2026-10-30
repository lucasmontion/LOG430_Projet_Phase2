package ca.ets.log430.candronex.slice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Test d'intégration exigé par l'énoncé : réseau simulé qui échoue toujours (MOCK_TAUX_ECHEC = 100 %). */
@TestPropertySource(properties = "candronex.mock.taux-echec=1.0")
class ActivationEchecIntegrationTest extends IntegrationSliceBase {

    @Test
    void avecUnTauxDEchecDe100PourcentLeServicePasseEnEchec() throws Exception {
        String serviceId = champ(demanderActivation(INSPECTRA, UUID.randomUUID(), UUID.randomUUID())
                .andExpect(status().isAccepted()), "$.serviceId");

        attendreEtat(serviceId, "ECHEC");

        consulter(INSPECTRA, serviceId)
                .andExpect(jsonPath("$.activatedAt").doesNotExist())
                .andExpect(jsonPath("$.causeEchec").value(containsString("MOCK_TAUX_ECHEC")));
    }
}