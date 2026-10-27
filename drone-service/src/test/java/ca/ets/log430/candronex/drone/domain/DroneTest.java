package ca.ets.log430.candronex.drone.domain;

import ca.ets.log430.candronex.commun.domaine.ClientId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DroneTest {

    private final ClientId client = new ClientId("CLIENT-001");

    @Test
    void unNouveauDroneEstActifEtAppartientAuClient() {
        Drone drone = Drone.enregistrer(new DroneId("DRONE-001"), client,
                new Imsi("999700000010231"), new Iccid("8999700000000010231"), TypeSim.SIM);

        assertThat(drone.estActif()).isTrue();
        assertThat(drone.appartientA(client)).isTrue();
        assertThat(drone.appartientA(new ClientId("CLIENT-002"))).isFalse();
    }
}
