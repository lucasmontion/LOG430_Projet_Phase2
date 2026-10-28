package ca.ets.log430.candronex.commande.adapter.out.drone;

/** drone-service ne répond pas (délai dépassé, refus de connexion, erreur 5xx) → 503. */
public class DroneServiceIndisponibleException extends RuntimeException {
    public DroneServiceIndisponibleException() {
        super("Le service des drones est momentanément indisponible");
    }
}