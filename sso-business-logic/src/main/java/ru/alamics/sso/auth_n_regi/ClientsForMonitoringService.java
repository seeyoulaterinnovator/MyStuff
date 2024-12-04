package ru.alamics.sso.auth_n_regi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.alamics.sso.jpa.entity.auth_reg.ClientsForMonitoringEntity;
import ru.alamics.sso.jpa.repository.ClientsForMonitoringRepository;
import ru.alamics.sso.keycloak.lookup.Lookup;

@ApplicationScoped
public class ClientsForMonitoringService {
    @Inject
    ClientsForMonitoringRepository clientsForMonitoringRepository;

    public ClientsForMonitoringService() {
        this.clientsForMonitoringRepository = Lookup.lookup(ClientsForMonitoringRepository.class);
    }

    public ClientsForMonitoringEntity findAndReturnClientsForMonitoringEntity(String realm, String client) {
        return clientsForMonitoringRepository.findAndReturnClientForMonitoring(client, realm);
    }
}
