package ca.ets.log430.candronex.catalogue.adapter.out.persistence;

import ca.ets.log430.candronex.catalogue.domain.Caracteristiques;
import ca.ets.log430.candronex.catalogue.domain.SpecificationService;
import ca.ets.log430.candronex.catalogue.domain.TypeService;
import org.springframework.stereotype.Component;

/** Traduit le modèle de persistance vers le modèle de domaine (lecture seule en Phase 1). */
@Component
public class SpecificationMapper {

    public SpecificationService versDomaine(SpecificationJpaEntity e) {
        return SpecificationService.reconstituer(
                TypeService.valueOf(e.getTypeService()),
                e.getNom(),
                new Caracteristiques(e.getSst(), e.getSd(), e.getDnn(), e.getFiveQi(), e.getArp(),
                        e.getAmbrMontant(), e.getAmbrDescendant()));
    }
}
