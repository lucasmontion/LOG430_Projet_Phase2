package ca.ets.log430.candronex.commande.adapter.out.persistence;

import ca.ets.log430.candronex.commande.domain.*;
import ca.ets.log430.candronex.commun.domaine.ClientId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommandeMapperTest {

    private final CommandeMapper mapper = new CommandeMapper();

    @Test
    void allerRetourConserveLaCommandeEtSesElementsDansLOrdre() {
        Commande originale = Commande.creer(new ClientId("CLIENT-001"), new CleIdempotence("cle-1"), List.of(
                ElementCommande.nouveau(new DroneId("DRONE-001"), new TypeService("C2"),
                        new CaracteristiquesFigees(2, "000001", "c2", 7, 2, "20 Mbps", "20 Mbps")),
                ElementCommande.nouveau(new DroneId("DRONE-001"), new TypeService("IMAGERIE"),
                        new CaracteristiquesFigees(1, "000002", "imagery", 9, 8, "500 Mbps", "100 Mbps"))));

        Commande relue = mapper.versDomaine(mapper.versEntite(originale));

        assertThat(relue.id()).isEqualTo(originale.id());
        assertThat(relue.clientId()).isEqualTo(originale.clientId());
        assertThat(relue.etat()).isEqualTo(EtatCommande.RECUE);
        assertThat(relue.elements()).extracting(e -> e.typeService().valeur()).containsExactly("C2", "IMAGERIE");
        assertThat(relue.elements().get(0).caracteristiques()).isEqualTo(originale.elements().get(0).caracteristiques());
    }
}
