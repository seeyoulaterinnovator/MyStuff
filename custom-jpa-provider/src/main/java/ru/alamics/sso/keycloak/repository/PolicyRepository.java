package ru.alamics.sso.keycloak.repository;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.RealmEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
@LocalBean
@Slf4j
public class PolicyRepository {

    @PersistenceContext
    private EntityManager em;

    public List<RealmEntity> findRealmWithPolicy(final String policy) {
      final String DEBUG_STR = "findRealmWithPolicy";

      List<RealmEntity> ret = em.createQuery("select re from RealmEntity re where re.passwordPolicy is not null ", RealmEntity.class)
              .getResultStream()
              .filter(realm -> realm.getPasswordPolicy().contains(policy))
              .collect(Collectors.toList());


      return ret;
    }
}
