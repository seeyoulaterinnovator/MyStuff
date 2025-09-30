package ru.alamics.sso.registration.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import ru.alamics.sso.jpa.entity.BrandEntity;
import ru.alamics.sso.jpa.entity.RealmBrandEntity;
import ru.alamics.sso.jpa.repository.BrandRepository;
import ru.alamics.sso.jpa.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BrandService {

    @Inject
    BrandRepository brandRepository;
    @Inject
    UserRepository userRepository;

    public List<BrandEntity> getAllBrands() {
        return brandRepository.findAll();
    }

    public List<RealmBrandEntity> getBrandsForRealm(String realmId) {
        return brandRepository.findByRealm(realmId);
    }

    public Optional<BrandEntity> getDefaultBrandByRealm(String realmId) {
        return brandRepository.findDefaultByRealm(realmId);
    }

    public Optional<BrandEntity> getBrandById(String brandId) {
        return brandRepository.findById(brandId);
    }

    public Optional<BrandEntity> getBrandByCode(String brandCode) {
        return brandRepository.findByCode(brandCode);
    }

    @Transactional
    public void attachBrandToRealm(String realmId, String brandId, boolean makeDefault) {
        brandRepository.findById(brandId)
                .orElseThrow(() -> new NotFoundException("Brand not found: " + brandId));

        if (brandRepository.isBrandInRealm(realmId, brandId)) {
            return;
        }

        brandRepository.addBrandToRealm(realmId, brandId, makeDefault);

        if (makeDefault) {
            brandRepository.setDefaultBrand(realmId, brandId);
        }
    }

    @Transactional
    public void setDefaultBrand(String realmId, String brandId) {
        if (!isBrandInRealm(realmId, brandId)) {
            throw new NotFoundException("Brand is not attached to realm");
        }
        brandRepository.setDefaultBrand(realmId, brandId);
    }

    @Transactional
    public void removeBrandFromRealm(String realmId, String brandId) {
        long countMarkBrandIds = userRepository.countByMarkBrandId(brandId);
        if (countMarkBrandIds > 0) {
            throw new IllegalStateException(
                    "Нельзя удалить бренд " + brandId + ", так как он используется у " + countMarkBrandIds + " пользователей."
            );
        }
        brandRepository.removeBrandFromRealm(realmId, brandId);
    }

    public boolean isBrandInRealm(String realmId, String brandId) {
        return brandRepository.isBrandInRealm(realmId, brandId);
    }
}
