package ca.ets.log430.candronex.slice.application;

import ca.ets.log430.candronex.commun.domaine.ClientId;
import ca.ets.log430.candronex.slice.domain.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Adaptateur de test branché sur le port ServiceRepository. */
class ServiceRepositoryEnMemoire implements ServiceRepository {
    final Map<ServiceId, Service> services = new LinkedHashMap<>();

    @Override
    public Service enregistrerSiAbsent(Service service) {
        return trouverParOrigine(service.origine()).orElseGet(() -> {
            services.put(service.id(), service);
            return service;
        });
    }

    @Override
    public void sauvegarder(Service service) { services.put(service.id(), service); }

    @Override
    public Optional<Service> trouver(ServiceId id) { return Optional.ofNullable(services.get(id)); }

    @Override
    public Optional<Service> trouverParOrigine(OrigineCommande origine) {
        return services.values().stream().filter(s -> s.origine().equals(origine)).findFirst();
    }

    @Override
    public Optional<Service> trouverPourClient(ClientId clientId, ServiceId id) {
        return trouver(id).filter(s -> s.appartientA(clientId));
    }

    @Override
    public List<Service> trouverParCommande(ClientId clientId, UUID commandeId) {
        return services.values().stream()
                .filter(s -> s.appartientA(clientId) && s.origine().commandeId().equals(commandeId))
                .toList();
    }
}
