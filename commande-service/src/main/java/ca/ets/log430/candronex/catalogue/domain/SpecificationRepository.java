package ca.ets.log430.candronex.catalogue.domain;

import java.util.Optional;

/** Port de sortie : lecture des spécifications du catalogue. */
public interface SpecificationRepository {
    Optional<SpecificationService> trouver(TypeService type);
}
