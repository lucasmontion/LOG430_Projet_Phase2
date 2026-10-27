package ca.ets.log430.candronex.drone.domain;

import ca.ets.log430.candronex.commun.domaine.ClientId;

import java.util.Objects;

/**
 * Racine de l'agrégat Drone. Classe Java pure : aucune annotation JPA (ADR-0002).
 */
public class Drone {
    private final DroneId id;
    private final ClientId clientId;
    private final Imsi imsi;
    private final Iccid iccid;
    private final TypeSim typeSim;
    private StatutDrone statut;

    private Drone(DroneId id, ClientId clientId, Imsi imsi, Iccid iccid, TypeSim typeSim, StatutDrone statut) {
        this.id = Objects.requireNonNull(id, "id obligatoire");
        this.clientId = Objects.requireNonNull(clientId, "clientId obligatoire");
        this.imsi = Objects.requireNonNull(imsi, "imsi obligatoire");
        this.iccid = Objects.requireNonNull(iccid, "iccid obligatoire");
        this.typeSim = Objects.requireNonNull(typeSim, "typeSim obligatoire");
        this.statut = Objects.requireNonNull(statut, "statut obligatoire");
    }

    /** UC-02 : un nouveau drone est toujours enregistré au statut ACTIF. */
    public static Drone enregistrer(DroneId id, ClientId clientId, Imsi imsi, Iccid iccid, TypeSim typeSim) {
        return new Drone(id, clientId, imsi, iccid, typeSim, StatutDrone.ACTIF);
    }

    /** Recrée un drone existant depuis la persistance, sans réappliquer les règles de création. */
    public static Drone reconstituer(DroneId id, ClientId clientId, Imsi imsi, Iccid iccid, TypeSim typeSim, StatutDrone statut) {
        return new Drone(id, clientId, imsi, iccid, typeSim, statut);
    }

    public boolean appartientA(ClientId client) {
        return clientId.equals(client);
    }

    public boolean estActif() {
        return statut == StatutDrone.ACTIF;
    }

    public DroneId id() { return id; }
    public ClientId clientId() { return clientId; }
    public Imsi imsi() { return imsi; }
    public Iccid iccid() { return iccid; }
    public TypeSim typeSim() { return typeSim; }
    public StatutDrone statut() { return statut; }
}
