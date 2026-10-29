package ca.ets.log430.candronex.slice.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Modèle de persistance d'un service de l'inventaire (base de slice-service).
 * commande_id, element_id et drone_id sont de simples colonnes : ces objets appartiennent à d'autres services.
 * L'IMSI n'est jamais stocké ici.
 */
@Entity
@Table(name = "service")
public class ServiceJpaEntity {
    @Id
    private UUID id;

    @Column(name = "commande_id", nullable = false) private UUID commandeId;
    @Column(name = "element_id", nullable = false) private UUID elementId;
    @Column(name = "client_id", nullable = false) private String clientId;
    @Column(name = "drone_id", nullable = false) private String droneId;
    @Column(name = "type_service", nullable = false) private String typeService;

    @Column(nullable = false) private int sst;
    @Column(nullable = false) private String sd;
    @Column(nullable = false) private String dnn;
    @Column(name = "five_qi", nullable = false) private int fiveQi;
    @Column(nullable = false) private int arp;
    @Column(name = "ambr_montant", nullable = false) private String ambrMontant;
    @Column(name = "ambr_descendant", nullable = false) private String ambrDescendant;

    @Column(nullable = false) private String etat;
    @Column(name = "accepted_at", nullable = false) private Instant acceptedAt;
    @Column(name = "activated_at") private Instant activatedAt;
    @Column(name = "cause_echec") private String causeEchec;

    protected ServiceJpaEntity() {}

    public ServiceJpaEntity(UUID id, UUID commandeId, UUID elementId, String clientId, String droneId,
                            String typeService, int sst, String sd, String dnn, int fiveQi, int arp,
                            String ambrMontant, String ambrDescendant, String etat,
                            Instant acceptedAt, Instant activatedAt, String causeEchec) {
        this.id = id;
        this.commandeId = commandeId;
        this.elementId = elementId;
        this.clientId = clientId;
        this.droneId = droneId;
        this.typeService = typeService;
        this.sst = sst;
        this.sd = sd;
        this.dnn = dnn;
        this.fiveQi = fiveQi;
        this.arp = arp;
        this.ambrMontant = ambrMontant;
        this.ambrDescendant = ambrDescendant;
        this.etat = etat;
        this.acceptedAt = acceptedAt;
        this.activatedAt = activatedAt;
        this.causeEchec = causeEchec;
    }

    public UUID getId() { return id; }
    public UUID getCommandeId() { return commandeId; }
    public UUID getElementId() { return elementId; }
    public String getClientId() { return clientId; }
    public String getDroneId() { return droneId; }
    public String getTypeService() { return typeService; }
    public int getSst() { return sst; }
    public String getSd() { return sd; }
    public String getDnn() { return dnn; }
    public int getFiveQi() { return fiveQi; }
    public int getArp() { return arp; }
    public String getAmbrMontant() { return ambrMontant; }
    public String getAmbrDescendant() { return ambrDescendant; }
    public String getEtat() { return etat; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public Instant getActivatedAt() { return activatedAt; }
    public String getCauseEchec() { return causeEchec; }
}
