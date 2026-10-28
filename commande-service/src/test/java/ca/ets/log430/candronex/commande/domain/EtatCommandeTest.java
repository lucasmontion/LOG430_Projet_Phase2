package ca.ets.log430.candronex.commande.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Le tableau de cas concrets du §6.2, transformé en tests. */
class EtatCommandeTest {

    private ElementCommande element(String type, EtatElement etat) {
        return ElementCommande.reconstituer(ElementId.nouveau(), new DroneId("DRONE-001"), new TypeService(type),
                CommandeTest.C2, etat);
    }

    @DisplayName("C2 + IMAGERIE -> état global")
    @ParameterizedTest(name = "{0} + {1} -> {2}")
    @CsvSource({
            "RECU,     RECU,     RECUE",
            "EN_COURS, RECU,     EN_COURS",
            "ACTIF,    EN_COURS, EN_COURS",
            "ACTIF,    ACTIF,    COMPLETEE",
            "ECHEC,    ACTIF,    PARTIELLE",   // le cas critique : un échec du C2 n'est jamais masqué
            "ACTIF,    ANNULE,   PARTIELLE",
            "ECHEC,    ECHEC,    ECHOUEE",
            "ANNULE,   ANNULE,   ANNULEE",
            "ACTIF,    COMPENSE, PARTIELLE",
            "COMPENSE, COMPENSE, ANNULEE"
    })
    void etatGlobalDeriveDesElements(EtatElement c2, EtatElement imagerie, EtatCommande attendu) {
        List<ElementCommande> elements = List.of(element("C2", c2), element("IMAGERIE", imagerie));

        assertThat(Commande.calculerEtat(elements)).isEqualTo(attendu);
    }
}
