package ca.ets.log430.candronex.drone.adapter.in.web;

import ca.ets.log430.candronex.drone.domain.Drone;

/**
 * DTO de la route interne identite-reseau, destinée à slice-service seulement (UC-06).
 * Contient l'IMSI : ne doit jamais être exposé par la passerelle.
 */
public record IdentiteReseauResponse(String droneId, String imsi, String typeSim, String statut) {
    public static IdentiteReseauResponse depuis(Drone drone) {
        return new IdentiteReseauResponse(drone.id().valeur(), drone.imsi().valeur(),
                drone.typeSim().name(), drone.statut().name());
    }
}
