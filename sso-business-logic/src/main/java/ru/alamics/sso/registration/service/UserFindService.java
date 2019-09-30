package ru.alamics.sso.registration.service;

import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.repository.UserRepository;
import ru.alamics.sso.util.Util;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class UserFindService {

    @EJB
    private UserRepository userRepository;

    public UserEntity getUserByPhone(String phone) {
        phone = Util.getCleanUserPhone(phone);
        if (phone != null) {
            return userRepository.getUserByPhoneNumber(phone);
        }
        return null;
    }
}
