package ca.ets.log430.candronex.slice.adapter.out.reseau;

import ca.ets.log430.candronex.slice.domain.ConfigurationSlice;
import ca.ets.log430.candronex.slice.domain.IdentiteReseau;
import ca.ets.log430.candronex.slice.domain.ResultatProvisionnement.Issue;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Le mock doit être réaliste : latence configurable, échecs selon le taux, délai dépassé = issue indéterminée. */
class MockOpen5GSAdapterTest {

    private static final IdentiteReseau ABONNE = new IdentiteReseau("999700000010231");
    private static final ConfigurationSlice C2 = new ConfigurationSlice(2, "000001", "c2", 7, 2, "20 Mbps", "20 Mbps");

    private MockOpen5GSAdapter mock(long min, long max, double tauxEchec, long delaiAttente) {
        return new MockOpen5GSAdapter(min, max, tauxEchec, delaiAttente, new Random(42));
    }

    @Test
    void tauxDEchecNulDonneToujoursUnSucces() { // valeur par défaut de l'énoncé : 0 %
        MockOpen5GSAdapter reseau = mock(1, 5, 0.0, 1000);

        for (int i = 0; i < 20; i++) {
            assertThat(reseau.appliquer(ABONNE, C2).issue()).isEqualTo(Issue.SUCCES);
        }
    }

    @Test
    void tauxDEchecForceACentPourCentDonneToujoursUnEchecCertain() { // scénario de test exigé par l'énoncé
        MockOpen5GSAdapter reseau = mock(1, 5, 1.0, 1000);

        for (int i = 0; i < 20; i++) {
            var resultat = reseau.appliquer(ABONNE, C2);
            assertThat(resultat.issue()).isEqualTo(Issue.ECHEC);
            assertThat(resultat.cause()).contains("Échec simulé");
        }
    }

    @Test
    void uneLatenceSuperieureAuDelaiDAttenteDonneUneIssueIndeterminee() {
        MockOpen5GSAdapter reseau = mock(50, 60, 0.0, 10);   // le réseau répond toujours après le délai

        long debut = System.nanoTime();
        var resultat = reseau.appliquer(ABONNE, C2);
        long dureeMs = (System.nanoTime() - debut) / 1_000_000;

        assertThat(resultat.issue()).isEqualTo(Issue.INDETERMINE);
        assertThat(dureeMs).isLessThan(50);   // l'appel est abandonné au délai, sans attendre la réponse
    }

    @Test
    void laLatenceSimuleeResteDansLIntervalleConfigure() {
        MockOpen5GSAdapter reseau = mock(20, 40, 0.0, 1000);

        long debut = System.nanoTime();
        reseau.appliquer(ABONNE, C2);
        long dureeMs = (System.nanoTime() - debut) / 1_000_000;

        assertThat(dureeMs).isBetween(20L, 200L);   // borne haute large : la machine de test peut être lente
    }

    @Test
    void refuseUneConfigurationIncoherente() {
        assertThatThrownBy(() -> mock(500, 100, 0.0, 1000)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mock(1, 5, 1.5, 1000)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void neRevelePasLImsiDansLesJournaux() {
        assertThat(ABONNE.toString()).isEqualTo("99970*******231").doesNotContain("0000010");
    }
}
