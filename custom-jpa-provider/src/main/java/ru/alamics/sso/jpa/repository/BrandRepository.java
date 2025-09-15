package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import ru.alamics.sso.jpa.entity.BrandEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BrandRepository {

    @PersistenceContext
    private EntityManager em;

    public List<BrandEntity> findByRealm(String realm) {
        return em.createQuery("SELECT b FROM BrandEntity b WHERE b.realm = :realm", BrandEntity.class)
                .setParameter("realm", realm)
                .getResultList();
    }

    public Optional<BrandEntity> findDefaultByRealm(String realm) {
        if (realm == null || realm.isBlank()) {
            return Optional.empty();
        }
        return em.createQuery(
                        "SELECT b FROM BrandEntity b WHERE b.realm = :realm AND b.isDefault = true",
                        BrandEntity.class)
                .setParameter("realm", realm)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public Optional<BrandEntity> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(em.find(BrandEntity.class, id));
    }

    @Transactional
    public void save(BrandEntity brand) {
        if (brand.getId() == null) {
            brand.setId(UUID.randomUUID().toString());
            em.persist(brand);
        } else {
            em.merge(brand);
        }
    }

    @Transactional
    public void deleteById(String id) {
        findById(id).ifPresent(em::remove);
    }

}
