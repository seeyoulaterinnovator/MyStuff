package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import ru.alamics.sso.jpa.entity.auth_reg.ClientsForMonitoringEntity;

import java.util.List;

@ApplicationScoped
public class ClientsForMonitoringRepository {
    @Inject
    private EntityManager em;

    public ClientsForMonitoringEntity findAndReturnClientForMonitoring(String clientName, String realm) {
        List<ClientsForMonitoringEntity> resultList = em.createQuery("select cfme from ClientsForMonitoringEntity cfme where cfme.clientName = :clientName and cfme.realm = :realm", ClientsForMonitoringEntity.class)
                .setParameter("clientName", clientName)
                .setParameter("realm", realm)
                .getResultList();

        if (resultList.isEmpty()) {
            return null;
        } else {
            return resultList.get(0);
        }
    }

}
