package ca.ets.log430.candronex.drone.application;

import ca.ets.log430.candronex.commun.domaine.ClientId;
import ca.ets.log430.candronex.drone.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service applicatif du module drone : coordonne le cas d'utilisation,
 * le domaine et le port de persistance. Les règles restent dans le domaine.
 */
@Service
public class DroneService {
    private static final Logger log = LoggerFactory.getLogger(DroneService.class);

    private final DroneRepository droneRepository;

    public DroneService(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    /** UC-02 : enregistrer un drone et son identité réseau. */
    public Drone enregistrer(EnregistrerDroneCommand commande) {
        // 1. Validation du format : les objets valeur refusent toute donnée invalide
        ClientId clientId = new ClientId(commande.clientId());
        DroneId droneId = new DroneId(commande.droneId());
        Imsi imsi = new Imsi(commande.imsi());
        Iccid iccid = new Iccid(commande.iccid());
        TypeSim typeSim = TypeSim.depuis(commande.typeSim());
        log.info("[application] DroneService.enregistrer clientId={} droneId={} imsi={}",
                clientId.valeur(), droneId.valeur(), imsi); // imsi masqué par toString()

        // 2. Unicité : droneId unique chez le client, IMSI unique dans tout le système
        if (droneRepository.existe(clientId, droneId)) {
            throw new DroneDejaEnregistreException(droneId);
        }
        if (droneRepository.imsiDejaUtilise(imsi)) {
            throw new ImsiDejaUtiliseException();
        }
        if (droneRepository.iccidDejaUtilise(iccid)) {
            throw new IccidDejaUtiliseException();
        }

        // 3. Création par le domaine, puis sauvegarde par le port
        Drone drone = Drone.enregistrer(droneId, clientId, imsi, iccid, typeSim);
        droneRepository.sauvegarder(drone);
        log.info("[application] drone enregistré droneId={} statut={}", droneId.valeur(), drone.statut());
        return drone;
    }

    /** Consultation d'un drone, toujours filtrée par client. */
    public Drone consulter(String clientId, String droneId) {
        DroneId id = new DroneId(droneId);
        return droneRepository.trouver(new ClientId(clientId), id)
                .orElseThrow(() -> new DroneIntrouvableException(id));
    }
}
