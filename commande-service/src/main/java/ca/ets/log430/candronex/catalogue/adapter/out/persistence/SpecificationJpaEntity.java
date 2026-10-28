package ca.ets.log430.candronex.catalogue.adapter.out.persistence;

import jakarta.persistence.*;

/** Modèle de persistance d'une spécification du catalogue (base de commande-service). */
@Entity
@Table(name = "specification_service")
public class SpecificationJpaEntity {
    @Id
    @Column(name = "type_service")
    private String typeService;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private int sst;

    @Column(nullable = false)
    private String sd;

    @Column(nullable = false)
    private String dnn;

    @Column(name = "five_qi", nullable = false)
    private int fiveQi;

    @Column(nullable = false)
    private int arp;

    @Column(name = "ambr_montant", nullable = false)
    private String ambrMontant;

    @Column(name = "ambr_descendant", nullable = false)
    private String ambrDescendant;

    protected SpecificationJpaEntity() {}

    public String getTypeService() { return typeService; }
    public String getNom() { return nom; }
    public int getSst() { return sst; }
    public String getSd() { return sd; }
    public String getDnn() { return dnn; }
    public int getFiveQi() { return fiveQi; }
    public int getArp() { return arp; }
    public String getAmbrMontant() { return ambrMontant; }
    public String getAmbrDescendant() { return ambrDescendant; }
}
