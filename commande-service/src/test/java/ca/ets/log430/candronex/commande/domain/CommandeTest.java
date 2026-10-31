package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.ClientId;
import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandeTest {

    static final CaracteristiquesFigees C2 = new CaracteristiquesFigees(2, "000001", "c2", 7, 2, "20 Mbps", "20 Mbps");
    static final CaracteristiquesFigees IMG = new CaracteristiquesFigees(1, "000002", "imagery", 9, 8, "500 Mbps", "100 Mbps");

    private final ClientId client = new ClientId("CLIENT-001");
    private final CleIdempotence cle = new CleIdempotence("cle-1");

    private ElementCommande element(String drone, String type, CaracteristiquesFigees c) {
        return ElementCommande.nouveau(new DroneId(drone), new TypeService(type), c);
    }

    @Test
    void uneNouvelleCommandeEstRecueEtSesElementsSontRecus() {
        Commande commande = Commande.creer(client, cle, List.of(
                element("DRONE-001", "C2", C2), element("DRONE-001", "IMAGERIE", IMG)));

        assertThat(commande.etat()).isEqualTo(EtatCommande.RECUE);
        assertThat(commande.elements()).extracting(ElementCommande::etat).containsOnly(EtatElement.RECU);
    }

    @Test
    void refuseUneCommandeSansElement() {
        assertThatThrownBy(() -> Commande.creer(client, cle, List.of()))
                .isInstanceOf(DonneeInvalideException.class)
                .hasMessageContaining("au moins un élément");
    }

    @Test
    void refuseDeuxFoisLeMemeServicePourLeMemeDrone() {
        assertThatThrownBy(() -> Commande.creer(client, cle, List.of(
                element("DRONE-001", "C2", C2), element("DRONE-001", "C2", C2))))
                .isInstanceOf(ElementEnDoubleException.class);
    }

    @Test
    void accepteLeMemeServicePourDeuxDronesDifferents() {
        Commande commande = Commande.creer(client, cle, List.of(
                element("DRONE-001", "C2", C2), element("DRONE-002", "C2", C2)));

        assertThat(commande.elements()).hasSize(2);
    }
}
