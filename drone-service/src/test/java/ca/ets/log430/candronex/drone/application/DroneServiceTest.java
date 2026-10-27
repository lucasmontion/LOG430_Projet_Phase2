package ca.ets.log430.candronex.drone.application;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;
import ca.ets.log430.candronex.drone.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/** Tests unitaires de UC-02 : scénario principal et scénarios alternatifs. */
class DroneServiceTest {

    private DroneService service;

    @BeforeEach
    void preparer() {
        service = new DroneService(new DroneRepositoryEnMemoire());
    }

    /** ICCID par défaut : pour les tests où il n'est pas la règle vérifiée. */
    private EnregistrerDroneCommand commande(String client, String droneId, String imsi) {
        return commande(client, droneId, imsi, "8999700000000010231", "SIM");
    }

    private EnregistrerDroneCommand commande(String client, String droneId, String imsi, String iccid) {
        return new EnregistrerDroneCommand(client, droneId, imsi, iccid, "SIM");
    }

    private EnregistrerDroneCommand commande(String client, String droneId, String imsi, String iccid, String typeSim) {
        return new EnregistrerDroneCommand(client, droneId, imsi, iccid, typeSim);
    }

    @Test
    void enregistreUnDroneValide() {
        Drone drone = service.enregistrer(commande("CLIENT-001", "DRONE-001", "999700000010231"));

        assertThat(drone.statut()).isEqualTo(StatutDrone.ACTIF);
        assertThat(service.consulter("CLIENT-001", "DRONE-001").id().valeur()).isEqualTo("DRONE-001");
    }

    @Test
    void refuseUnImsiMalForme() { // flux 2a
        assertThatThrownBy(() -> service.enregistrer(commande("CLIENT-001", "DRONE-001", "123")))
                .isInstanceOf(DonneeInvalideException.class);
    }

    @Test
    void refuseUnDroneIdDejaEnregistreChezLeClient() { // flux 3a
        service.enregistrer(commande("CLIENT-001", "DRONE-001", "999700000010231"));

        assertThatThrownBy(() -> service.enregistrer(commande("CLIENT-001", "DRONE-001", "999700000010999")))
                .isInstanceOf(DroneDejaEnregistreException.class);
    }

    @Test
    void refuseUnImsiDejaUtiliseParUnAutreClient() { // flux 3b
        service.enregistrer(commande("CLIENT-001", "DRONE-001", "999700000010231", "8999700000000000001"));

        assertThatThrownBy(() -> service.enregistrer(commande("CLIENT-002", "DRONE-777", "999700000010231", "8999700000000000002")))
                .isInstanceOf(ImsiDejaUtiliseException.class);
    }

    @Test
    void refuseUnIccidDejaUtiliseParUnAutreClient() { // flux 3c
        service.enregistrer(commande("CLIENT-001", "DRONE-001", "999700000010232", "8999700000000000001"));

        assertThatThrownBy(() -> service.enregistrer(commande("CLIENT-002", "DRONE-777", "999700000010231", "8999700000000000001")))
                .isInstanceOf(IccidDejaUtiliseException.class);
    }

    @Test
    void refuseUnSimTypeInvalide() { // flux 2b
        assertThatThrownBy(() -> service.enregistrer(commande("CLIENT-001", "DRONE-001", "999700000010231", "8999700000000010231", "OTHER")))
                .isInstanceOf(DonneeInvalideException.class);
    }


    @Test
    void neMontrePasLeDroneDUnAutreClient() { // non-divulgation
        service.enregistrer(commande("CLIENT-001", "DRONE-001", "999700000010231"));

        assertThatThrownBy(() -> service.consulter("CLIENT-002", "DRONE-001"))
                .isInstanceOf(DroneIntrouvableException.class);
    }
}
