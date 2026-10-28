package ca.ets.log430.candronex.catalogue.api;

import ca.ets.log430.candronex.catalogue.application.CatalogueService;
import ca.ets.log430.candronex.catalogue.domain.*;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogueFacadeTest {

    private final SpecificationRepository repositoryEnMemoire = type -> Optional.ofNullable(Map.of(
            TypeService.C2, SpecificationService.reconstituer(TypeService.C2, "C&C Connectivity",
                    new Caracteristiques(2, "000001", "c2", 7, 2, "20 Mbps", "20 Mbps"))
    ).get(type));

    private final CatalogueFacade facade = new CatalogueFacade(new CatalogueService(repositoryEnMemoire));

    @Test
    void retourneLesCaracteristiquesDUnServiceConnu() {
        SpecificationDto c2 = facade.trouverSpecification("C2").orElseThrow();

        assertThat(c2.sst()).isEqualTo(2);
        assertThat(c2.sd()).isEqualTo("000001");
        assertThat(c2.arp()).isEqualTo(2);
    }

    @Test
    void retourneVidePourUnTypeAbsentDuCatalogue() {
        assertThat(facade.trouverSpecification("IMAGERIE")).isEmpty();
    }

    @Test
    void retourneVidePourUnTypeInexistant() {
        assertThat(facade.trouverSpecification("SATELLITE")).isEmpty();
    }
}
