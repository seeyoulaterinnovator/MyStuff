package ru.alamics.sso.jpa.repository;

import ru.alamics.sso.jpa.entity.auth_reg.ClientsForMonitoringEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@LocalBean
@Stateless
public class ClientsForMonitoringRepository {
    @PersistenceContext
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
