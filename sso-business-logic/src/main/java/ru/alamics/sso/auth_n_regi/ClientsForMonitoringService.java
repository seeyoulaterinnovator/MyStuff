package ru.alamics.sso.auth_n_regi;

import ru.alamics.sso.jpa.entity.auth_reg.ClientsForMonitoringEntity;
import ru.alamics.sso.jpa.repository.ClientsForMonitoringRepository;
import ru.alamics.sso.keycloak.lookup.Lookup;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class ClientsForMonitoringService {

    @EJB
    private final ClientsForMonitoringRepository clientsForMonitoringRepository;

    public ClientsForMonitoringService() {
        this.clientsForMonitoringRepository = Lookup.lookup(ClientsForMonitoringRepository.class);
    }

    public ClientsForMonitoringEntity findAndReturnClientsForMonitoringEntity(String realm, String client) {
        return clientsForMonitoringRepository.findAndReturnClientForMonitoring(client, realm);
    }
}
