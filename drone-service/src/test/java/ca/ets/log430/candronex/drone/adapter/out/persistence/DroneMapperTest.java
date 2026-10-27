package ca.ets.log430.candronex.drone.adapter.out.persistence;

import ca.ets.log430.candronex.commun.domaine.ClientId;
import ca.ets.log430.candronex.drone.domain.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DroneMapperTest {

    private final DroneMapper mapper = new DroneMapper();

    @Test
    void allerRetourConserveToutesLesDonnees() {
        Drone original = Drone.enregistrer(new DroneId("DRONE-001"), new ClientId("CLIENT-001"),
                new Imsi("999700000010231"), new Iccid("8999700000000010231"), TypeSim.SIM);

        Drone relu = mapper.versDomaine(mapper.versEntite(original));

        assertThat(relu.id()).isEqualTo(original.id());
        assertThat(relu.clientId()).isEqualTo(original.clientId());
        assertThat(relu.imsi()).isEqualTo(original.imsi());
        assertThat(relu.iccid()).isEqualTo(original.iccid());
        assertThat(relu.statut()).isEqualTo(StatutDrone.ACTIF);
    }
}
