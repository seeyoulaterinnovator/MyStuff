package ru.alamics.sso.registration.service;

import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.repository.UserRepository;
import ru.alamics.sso.util.Util;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class UserFindService {

    @EJB
    private UserRepository userRepository;

    public UserEntity getUserByPhone(RealmModel realm, String phone) {
        phone = Util.getCleanUserPhone(phone);
        if (phone != null) {
            return userRepository.getFirstUserByPhoneNumber(realm, phone, null);
        }
        return null;
    }

    public UserEntity getUserByPhoneAndExcludedUserId(RealmModel realm, String phone, String excludedUserId) {
        phone = Util.getCleanUserPhone(phone);
        if (phone != null) {
            return userRepository.getFirstUserByPhoneNumber(realm, phone, excludedUserId);
        }
        return null;
    }

    public UserEntity getUserByPhoneAndExcludedUserId(String phone, String excludedUserId) {
        phone = Util.getCleanUserPhone(phone);
        if (phone != null) {
            return userRepository.getFirstUserByPhoneNumber(phone, excludedUserId);
        }
        return null;
    }
}
