package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.keycloak.models.jpa.entities.RealmEntity;

import java.util.List;

@ApplicationScoped
public class PolicyRepository {
    @Inject
    private EntityManager em;

    public List<RealmEntity> findRealmWithPolicy(final String policy) {
        return em.createQuery(
                        "select re from RealmEntity re " +
                                "where re.passwordPolicy LIKE CONCAT('%', :policy, '%') ", RealmEntity.class)
                .setParameter("policy", policy)
                .getResultList();
    }

    public void findExpiredPasswords(final String realm, final long millis) {
        em.createNativeQuery("insert into AUTO_LOCK_NOTIFICATION(id, user_id, sended_at, type, status)\n" +
                        "SELECT uuid(), cred.USER_ID, null, 'PASSWORD_EXPIRED', 'PREPARE'\n" +
                        "from CREDENTIAL cred\n" +
                        "         join USER_ENTITY ue on cred.USER_ID = ue.ID\n" +
                        "where ((UNIX_TIMESTAMP() * 1000) - cred.CREATED_DATE) >= :millis\n" +
                        "  and (not exists(\n" +
                        "        select 1 from AUTO_LOCK_NOTIFICATION aln where aln.USER_ID = cred.USER_ID and aln.TYPE = 'PASSWORD_EXPIRED')\n" +
                        "    or cred.CREATED_DATE > unix_timestamp((select max(aln.SENDED_AT) over (PARTITION BY aln.USER_ID) date\n" +
                        "                                           from AUTO_LOCK_NOTIFICATION aln\n" +
                        "                                           where aln.USER_ID = cred.USER_ID\n" +
                        "                                             and aln.TYPE = 'PASSWORD_EXPIRED'\n" +
                        "                                             and aln.STATUS = 'SENT'\n" +
                        "                                           limit 1)) * 1000)\n" +
                        "  and ue.REALM_ID = :realm\n" +
                        " LIMIT 100 ")
                .setParameter("millis", millis)
                .setParameter("realm", realm)
                .executeUpdate();
    }
}
