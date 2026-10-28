package ca.ets.log430.candronex.catalogue.domain;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaracteristiquesTest {

    @Test
    void accepteLesValeursDeReferenceDuC2() {
        assertThatCode(() -> new Caracteristiques(2, "000001", "c2", 7, 2, "20 Mbps", "20 Mbps"))
                .doesNotThrowAnyException();
    }

    @Test
    void refuseUnSdQuiNEstPasHexadecimal() {
        assertThatThrownBy(() -> new Caracteristiques(2, "00000Z", "c2", 7, 2, "20 Mbps", "20 Mbps"))
                .isInstanceOf(DonneeInvalideException.class);
    }

    @Test
    void refuseUnArpHorsDeLEchelleUnAQuinze() {
        assertThatThrownBy(() -> new Caracteristiques(2, "000001", "c2", 7, 16, "20 Mbps", "20 Mbps"))
                .isInstanceOf(DonneeInvalideException.class);
    }

    @Test
    void refuseUnDebitMalForme() {
        assertThatThrownBy(() -> new Caracteristiques(2, "000001", "c2", 7, 2, "vingt", "20 Mbps"))
                .isInstanceOf(DonneeInvalideException.class);
    }
}
