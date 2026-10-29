package ca.ets.log430.candronex.slice.domain;

import java.util.Objects;

/**
 * Identité de l'abonné dans le réseau 5G (IMSI), résolue auprès de drone-service au moment de l'activation.
 * Jamais persistée dans slice-service ni renvoyée au client ; toString() est masqué pour les journaux.
 */
public record IdentiteReseau(String imsi) {
    public IdentiteReseau {
        Objects.requireNonNull(imsi, "imsi");
    }

    @Override
    public String toString() {
        return imsi.length() < 8 ? "****" : imsi.substring(0, 5) + "*******" + imsi.substring(imsi.length() - 3);
    }
}
