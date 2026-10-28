package ca.ets.log430.candronex.catalogue.domain;

import java.util.Objects;

/**
 * Racine de l'agrégat SpecificationService : un service offert au catalogue.
 * Le catalogue est piloté par les données (migration Flyway) : aucune création par l'API en Phase 1.
 */
public class SpecificationService {
    private final TypeService type;
    private final String nom;
    private final Caracteristiques caracteristiques;

    private SpecificationService(TypeService type, String nom, Caracteristiques caracteristiques) {
        this.type = Objects.requireNonNull(type, "type obligatoire");
        this.nom = Objects.requireNonNull(nom, "nom obligatoire");
        this.caracteristiques = Objects.requireNonNull(caracteristiques, "caracteristiques obligatoires");
    }

    public static SpecificationService reconstituer(TypeService type, String nom, Caracteristiques caracteristiques) {
        return new SpecificationService(type, nom, caracteristiques);
    }

    public TypeService type() { return type; }
    public String nom() { return nom; }
    public Caracteristiques caracteristiques() { return caracteristiques; }
}
