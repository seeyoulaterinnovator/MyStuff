package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import ru.alamics.sso.jpa.entity.BrandEntity;
import ru.alamics.sso.jpa.entity.RealmBrandEntity;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BrandRepository {

    @Inject
    EntityManager em;

    /**
     * Все привязки брендов к реалму (с подгрузкой бренда).
     */
    public List<RealmBrandEntity> findByRealm(String realmId) {
        return em.createQuery(
                        "SELECT rb FROM RealmBrandEntity rb " +
                                "JOIN FETCH rb.brand " +
                                "WHERE rb.realmId = :realmId", RealmBrandEntity.class)
                .setParameter("realmId", realmId)
                .getResultList();
    }

    /**
     * Найти бренд по UUID.
     */
    public Optional<BrandEntity> findById(String id) {
        return Optional.ofNullable(em.find(BrandEntity.class, id));
    }

    /**
     * Дефолтный бренд для реалма.
     */
    public Optional<BrandEntity> findDefaultByRealm(String realmId) {
        return em.createQuery(
                        "SELECT rb.brand FROM RealmBrandEntity rb " +
                                "WHERE rb.realmId = :realmId AND rb.isDefault = true", BrandEntity.class)
                .setParameter("realmId", realmId)
                .getResultStream()
                .findFirst();
    }

    /**
     * Есть ли связь realm <-> </->brand.
     */
    public boolean isBrandInRealm(String realmId, String brandId) {
        Long cnt = em.createQuery(
                        "SELECT COUNT(rb) FROM RealmBrandEntity rb " +
                                "WHERE rb.realmId = :realmId AND rb.brandId = :brandId", Long.class)
                .setParameter("realmId", realmId)
                .setParameter("brandId", brandId)
                .getSingleResult();
        return cnt != null && cnt > 0;
    }

    /**
     * Привязать бренд к реалму. Уникальный индекс на (REALM_ID, BRAND_ID) защитит от дублей.
     */
    @Transactional
    public void addBrandToRealm(String realmId, String brandId, boolean isDefault) {
        BrandEntity brand = em.find(BrandEntity.class, brandId);
        if (brand == null) {
            throw new IllegalArgumentException("Бренд с ID " + brandId + " не найден");
        }

        RealmBrandEntity rb = new RealmBrandEntity();
        rb.setRealmId(realmId);
        rb.setBrandId(brandId);
        rb.setIsDefault(isDefault);

        if (isDefault) {
            em.createQuery("UPDATE RealmBrandEntity rb SET rb.isDefault = false WHERE rb.realmId = :realmId")
                    .setParameter("realmId", realmId)
                    .executeUpdate();
        }

        em.persist(rb);
    }

    /**
     * Назначить дефолтный бренд для реалма (делаем единственным).
     */
    @Transactional
    public void setDefaultBrand(String realmId, String brandId) {
        em.createQuery("UPDATE RealmBrandEntity rb SET rb.isDefault = false WHERE rb.realmId = :realmId")
                .setParameter("realmId", realmId)
                .executeUpdate();

        int updated = em.createQuery(
                        "UPDATE RealmBrandEntity rb SET rb.isDefault = true " +
                                "WHERE rb.realmId = :realmId AND rb.brandId = :brandId")
                .setParameter("realmId", realmId)
                .setParameter("brandId", brandId)
                .executeUpdate();

        if (updated == 0) {
            throw new IllegalArgumentException("Связка realm=" + realmId + " и brandId=" + brandId + " не найдена");
        }
    }

    /**
     * Отвязать бренд от реалма.
     */
    @Transactional
    public void removeBrandFromRealm(String realmId, String brandId) {
        em.createQuery("DELETE FROM RealmBrandEntity rb " +
                        "WHERE rb.realmId = :realmId AND rb.brandId = :brandId")
                .setParameter("realmId", realmId)
                .setParameter("brandId", brandId)
                .executeUpdate();
    }
}