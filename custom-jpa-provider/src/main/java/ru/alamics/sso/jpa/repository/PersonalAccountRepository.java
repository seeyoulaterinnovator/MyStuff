package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.PersonalAccountEntity;
import ru.alamics.sso.jpa.entity.PersonalAccountPostEntity;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional(Transactional.TxType.REQUIRED)
@Slf4j
public class PersonalAccountRepository {
    @Inject
    private EntityManager em;

    public PersonalAccountPostEntity getAccount(final String postId) {

        return em.find(PersonalAccountPostEntity.class, postId);

    }

    public void setAccountList(final String postId, List<String> paList) {

        deleteAccountList(postId);

        PersonalAccountPostEntity pe = new PersonalAccountPostEntity();
        pe.setPostId(postId);

        em.persist(pe);


        if (paList == null)
            paList = new ArrayList<>();

        for (String pa : paList) {
            PersonalAccountEntity en = new PersonalAccountEntity(null, pe, pa);
            em.persist(en);
        }

        em.flush();
    }

    public void addAccountList(final String postId, List<String> paList) {

        PersonalAccountPostEntity pe = em.find(PersonalAccountPostEntity.class, postId);

        if (pe == null) {
            pe = new PersonalAccountPostEntity();
            pe.setPostId(postId);

            em.persist(pe);
            pe = em.find(PersonalAccountPostEntity.class, postId);
        }


        if (paList == null)
            paList = new LinkedList<>();
        Set<PersonalAccountEntity> paEnList = new HashSet<>();
        for (String pa : paList) {
            String[] accountsNumber = pa.split(",");
            for (String acn : Arrays.stream(accountsNumber)
                    .distinct().collect(Collectors.toList())) {

                List<PersonalAccountEntity> value = em.createQuery("SELECT pae FROM PersonalAccountEntity pae WHERE pae.value = :val and pae.post = :post"
                                , PersonalAccountEntity.class)
                        .setParameter("val", acn)
                        .setParameter("post", pe)
                        .getResultList();
                if (value.isEmpty()) {
                    PersonalAccountEntity en = new PersonalAccountEntity(null, pe, acn);
                    paEnList.add(en);
                }
            }
        }

        if (pe.getAccounts() != null)
            paEnList.addAll(pe.getAccounts());

        pe.setAccounts(paEnList);
        em.persist(pe); // ?

        em.flush();
    }

    public void subAccountUuidList(final String postId, List<String> paUuidList) {

        for (String uuid : paUuidList) {

            PersonalAccountEntity en = em.find(PersonalAccountEntity.class, uuid);
            if (en != null) {

                em.remove(en);
            }
        }

        em.flush();
    }


    public void deleteAccountList(final String postId) {

        PersonalAccountPostEntity pe = em.find(PersonalAccountPostEntity.class, postId);

        if (pe != null) {
            em.remove(pe);
            em.flush();
        }

    }
}
