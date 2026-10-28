package ca.ets.log430.candronex.commande.domain;

/** Ce que la commande retient d'un drone vérifié : jamais d'IMSI ni d'ICCID. */
public record DroneVerifie(DroneId droneId, String statut) {
    public boolean estActif() {
        return "ACTIF".equals(statut);
    }
}