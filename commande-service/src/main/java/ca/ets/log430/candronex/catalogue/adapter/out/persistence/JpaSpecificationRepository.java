package ca.ets.log430.candronex.catalogue.adapter.out.persistence;

import ca.ets.log430.candronex.catalogue.domain.SpecificationRepository;
import ca.ets.log430.candronex.catalogue.domain.SpecificationService;
import ca.ets.log430.candronex.catalogue.domain.TypeService;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Adaptateur de sortie : implémente le port SpecificationRepository avec JPA. */
@Repository
public class JpaSpecificationRepository implements SpecificationRepository {

    private final SpecificationJpaRepository jpa;
    private final SpecificationMapper mapper;

    JpaSpecificationRepository(SpecificationJpaRepository jpa, SpecificationMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SpecificationService> trouver(TypeService type) {
        return jpa.findById(type.name()).map(mapper::versDomaine);
    }
}
