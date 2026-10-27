package ca.ets.log430.candronex.drone.api;

/** Objet d'échange exposé aux autres modules : jamais d'IMSI ni d'ICCID. */
public record DroneRefDto(String droneId, String statut) {
    public boolean estActif() {
        return "ACTIF".equals(statut);
    }
}
