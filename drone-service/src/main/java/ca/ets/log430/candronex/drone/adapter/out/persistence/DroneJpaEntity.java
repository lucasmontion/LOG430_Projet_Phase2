package ca.ets.log430.candronex.drone.adapter.out.persistence;

import jakarta.persistence.*;

/**
 * Modèle de persistance du drone (distinct du modèle de domaine, ADR-0002).
 * Vit dans le schéma "drone" : seul ce module y accède.
 */
@Entity
@Table(name = "drone")
public class DroneJpaEntity {
    @EmbeddedId
    private DroneJpaId id;

    @Column(nullable = false, unique = true)
    private String imsi;

    @Column(nullable = false)
    private String iccid;

    @Column(nullable = false)
    private String statut;

    @Column(nullable = false)
    private String typeSim;

    protected DroneJpaEntity() {}

    public DroneJpaEntity(DroneJpaId id, String imsi, String iccid, String typeSim, String statut) {
        this.id = id;
        this.imsi = imsi;
        this.iccid = iccid;
        this.typeSim = typeSim;
        this.statut = statut;
    }

    public DroneJpaId getId() { return id; }
    public String getImsi() { return imsi; }
    public String getIccid() { return iccid; }
    public String getTypeSim() { return typeSim; }
    public String getStatut() { return statut; }
}
