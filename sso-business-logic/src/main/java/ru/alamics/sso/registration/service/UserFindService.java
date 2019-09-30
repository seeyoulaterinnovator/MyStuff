package ru.alamics.sso.registration.service;

import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.repository.UserRepository;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class UserFindService {

    @EJB
    private UserRepository userRepository;

    public UserEntity getUserByPhone(String phone) {
        return userRepository.getUserByPhoneNumber(phone);
    }
}
