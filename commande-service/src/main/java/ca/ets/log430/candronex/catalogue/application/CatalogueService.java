package ca.ets.log430.candronex.catalogue.application;

import ca.ets.log430.candronex.catalogue.domain.SpecificationRepository;
import ca.ets.log430.candronex.catalogue.domain.SpecificationService;
import ca.ets.log430.candronex.catalogue.domain.TypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Service applicatif du catalogue : consultation des spécifications. */
@Service
public class CatalogueService {
    private static final Logger log = LoggerFactory.getLogger(CatalogueService.class);

    private final SpecificationRepository repository;

    public CatalogueService(SpecificationRepository repository) {
        this.repository = repository;
    }

    public Optional<SpecificationService> consulter(TypeService type) {
        log.info("[application] CatalogueService.consulter type={}", type);
        return repository.trouver(type);
    }
}
