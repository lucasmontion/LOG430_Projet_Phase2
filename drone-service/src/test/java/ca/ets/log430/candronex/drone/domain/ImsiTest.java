package ca.ets.log430.candronex.drone.domain;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImsiTest {

    @Test
    void accepteUnImsiDeQuinzeChiffres() {
        assertThat(new Imsi("999700000010231").valeur()).isEqualTo("999700000010231");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"99970000001023", "9997000000102311", "99970000001023A", "999 700000010231"})
    void refuseUnImsiInvalide(String valeur) {
        assertThatThrownBy(() -> new Imsi(valeur)).isInstanceOf(DonneeInvalideException.class);
    }

    @Test
    void neRevelePasLImsiDansToString() {
        assertThat(new Imsi("999700000010231").toString())
                .isEqualTo("99970*******231")
                .doesNotContain("0000001");
    }
}
