package ca.ets.log430.candronex.slice.adapter.out.persistence;

import ca.ets.log430.candronex.commun.domaine.ClientId;
import ca.ets.log430.candronex.slice.domain.*;
import org.springframework.stereotype.Component;

/** Traduit entre l'agrégat Service (domaine) et son entité JPA (persistance). */
@Component
public class ServiceMapper {

    public ServiceJpaEntity versEntite(Service s) {
        ConfigurationSlice c = s.configuration();
        return new ServiceJpaEntity(
                s.id().valeur(), s.origine().commandeId(), s.origine().elementId(),
                s.clientId().valeur(), s.droneId().valeur(), s.typeService(),
                c.sst(), c.sd(), c.dnn(), c.fiveQi(), c.arp(), c.ambrMontant(), c.ambrDescendant(),
                s.etat().name(), s.acceptedAt(), s.activatedAt().orElse(null), s.causeEchec().orElse(null));
    }

    public Service versDomaine(ServiceJpaEntity e) {
        return Service.reconstituer(
                new ServiceId(e.getId()),
                new OrigineCommande(e.getCommandeId(), e.getElementId()),
                new ClientId(e.getClientId()),
                new DroneId(e.getDroneId()),
                e.getTypeService(),
                new ConfigurationSlice(e.getSst(), e.getSd(), e.getDnn(), e.getFiveQi(), e.getArp(),
                        e.getAmbrMontant(), e.getAmbrDescendant()),
                e.getAcceptedAt(),
                EtatService.valueOf(e.getEtat()),
                e.getActivatedAt(),
                e.getCauseEchec());
    }
}
