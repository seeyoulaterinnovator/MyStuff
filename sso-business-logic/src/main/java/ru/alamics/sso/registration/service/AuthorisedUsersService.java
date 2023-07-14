package ru.alamics.sso.registration.service;

import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.auth_reg.AuthorisedUsersEntity;
import ru.alamics.sso.jpa.repository.AuthorisedUsersRepository;
import ru.alamics.sso.jpa.repository.UserRepository;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.model.User;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.time.LocalDateTime;
import java.util.UUID;

@Stateless
@LocalBean
public class AuthorisedUsersService {

    @EJB
    private final AuthorisedUsersRepository authorisedUsersRepository;
    @EJB
    private final UserRepository userRepository;

    public AuthorisedUsersService() {
        this.authorisedUsersRepository = Lookup.lookup(AuthorisedUsersRepository.class);
        this.userRepository = Lookup.lookup(UserRepository.class);
    }

    public void saveSuccessfulAuth(User user, String realm, String authType) {
        UserEntity userEntity = userRepository.findUser(user.getId());
        AuthorisedUsersEntity authorisedUsersEntity = new AuthorisedUsersEntity();
        authorisedUsersEntity.setUser(userEntity);
        authorisedUsersEntity.setAuthType(authType);
        authorisedUsersEntity.setId(UUID.randomUUID().toString());
        authorisedUsersEntity.setRealm(realm);
        authorisedUsersEntity.setCreated(LocalDateTime.now());
        authorisedUsersRepository.save(authorisedUsersEntity);
    }
}
