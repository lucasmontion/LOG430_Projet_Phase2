package ca.ets.log430.candronex.drone.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/** Clé primaire composée : un droneId est unique chez un client. */
@Embeddable
public class DroneJpaId implements Serializable {
    @Column(name = "client_id")
    private String clientId;

    @Column(name = "drone_id")
    private String droneId;

    protected DroneJpaId() {}

    public DroneJpaId(String clientId, String droneId) {
        this.clientId = clientId;
        this.droneId = droneId;
    }

    public String getClientId() { return clientId; }
    public String getDroneId() { return droneId; }

    @Override
    public boolean equals(Object o) {
        return o instanceof DroneJpaId autre
                && Objects.equals(clientId, autre.clientId)
                && Objects.equals(droneId, autre.droneId);
    }

    @Override
    public int hashCode() { return Objects.hash(clientId, droneId); }
}
