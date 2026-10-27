package ca.ets.log430.candronex.drone.application;

/** Données d'entrée du cas d'utilisation UC-02, indépendantes de HTTP. */
public record EnregistrerDroneCommand(String clientId, String droneId, String imsi, String iccid, String typeSim) {}
