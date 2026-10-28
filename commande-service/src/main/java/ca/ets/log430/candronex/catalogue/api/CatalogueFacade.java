package ca.ets.log430.candronex.catalogue.api;

import ca.ets.log430.candronex.catalogue.application.CatalogueService;
import ca.ets.log430.candronex.catalogue.domain.Caracteristiques;
import ca.ets.log430.candronex.catalogue.domain.TypeService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * Interface publique du module catalogue : seul point d'entrée pour les autres modules.
 * Utilisée par le module commande (UC-04) pour copier les caractéristiques d'un service.
 */
@Component
public class CatalogueFacade {

    private final CatalogueService catalogueService;

    public CatalogueFacade(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    /** Retourne vide si le type de service n'existe pas au catalogue. */
    public Optional<SpecificationDto> trouverSpecification(String typeService) {
        return versType(typeService)
                .flatMap(catalogueService::consulter)
                .map(spec -> {
                    Caracteristiques c = spec.caracteristiques();
                    return new SpecificationDto(spec.type().name(), spec.nom(),
                            c.sst(), c.sd(), c.dnn(), c.fiveQi(), c.arp(), c.ambrMontant(), c.ambrDescendant());
                });
    }

    private Optional<TypeService> versType(String valeur) {
        return Arrays.stream(TypeService.values())
                .filter(t -> t.name().equals(valeur))
                .findFirst();
    }
}
