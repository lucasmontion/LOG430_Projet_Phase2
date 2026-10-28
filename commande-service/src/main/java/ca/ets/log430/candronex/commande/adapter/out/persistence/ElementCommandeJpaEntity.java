package ca.ets.log430.candronex.commande.adapter.out.persistence;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Modèle de persistance d'un élément de commande.
 * drone_id est une simple colonne, PAS une relation vers le module drone :
 * aucune relation JPA ne traverse une frontière de module.
 */
@Entity
@Table(name = "element_commande")
public class ElementCommandeJpaEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commande_id", nullable = false)
    private CommandeJpaEntity commande;

    @Column(nullable = false)
    private int rang;

    @Column(name = "drone_id", nullable = false)
    private String droneId;

    @Column(name = "type_service", nullable = false)
    private String typeService;

    @Column(nullable = false)
    private String etat;

    @Column(nullable = false) private int sst;
    @Column(nullable = false) private String sd;
    @Column(nullable = false) private String dnn;
    @Column(name = "five_qi", nullable = false) private int fiveQi;
    @Column(nullable = false) private int arp;
    @Column(name = "ambr_montant", nullable = false) private String ambrMontant;
    @Column(name = "ambr_descendant", nullable = false) private String ambrDescendant;

    protected ElementCommandeJpaEntity() {}

    public ElementCommandeJpaEntity(UUID id, int rang, String droneId, String typeService, String etat,
                                    int sst, String sd, String dnn, int fiveQi, int arp,
                                    String ambrMontant, String ambrDescendant) {
        this.id = id;
        this.rang = rang;
        this.droneId = droneId;
        this.typeService = typeService;
        this.etat = etat;
        this.sst = sst;
        this.sd = sd;
        this.dnn = dnn;
        this.fiveQi = fiveQi;
        this.arp = arp;
        this.ambrMontant = ambrMontant;
        this.ambrDescendant = ambrDescendant;
    }

    void rattacherA(CommandeJpaEntity commande) { this.commande = commande; }

    public UUID getId() { return id; }
    public int getRang() { return rang; }
    public String getDroneId() { return droneId; }
    public String getTypeService() { return typeService; }
    public String getEtat() { return etat; }
    public int getSst() { return sst; }
    public String getSd() { return sd; }
    public String getDnn() { return dnn; }
    public int getFiveQi() { return fiveQi; }
    public int getArp() { return arp; }
    public String getAmbrMontant() { return ambrMontant; }
    public String getAmbrDescendant() { return ambrDescendant; }
}
