package ru.alamics.sso.registration.service;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.auth_n_regi.ClientsForMonitoringService;
import ru.alamics.sso.jpa.entity.auth_reg.AuthOrRegTypeEntity;
import ru.alamics.sso.jpa.entity.auth_reg.AuthorisedUsersEntity;
import ru.alamics.sso.jpa.entity.auth_reg.ClientsForMonitoringEntity;
import ru.alamics.sso.jpa.repository.AuthorisedUsersRepository;
import ru.alamics.sso.jpa.repository.UserRepository;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.model.User;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Stateless
@LocalBean
@Slf4j
public class AuthorisedUsersService {

    @EJB
    private final AuthorisedUsersRepository authorisedUsersRepository;
    @EJB
    private final UserRepository userRepository;
    private final AuthOrRegTypeService authOrRegTypeService;

    private final ClientsForMonitoringService clientsForMonitoringService;


    public AuthorisedUsersService() {
        this.authorisedUsersRepository = Lookup.lookup(AuthorisedUsersRepository.class);
        this.userRepository = Lookup.lookup(UserRepository.class);
        this.authOrRegTypeService = Lookup.lookup(AuthOrRegTypeService.class);
        this.clientsForMonitoringService = Lookup.lookup(ClientsForMonitoringService.class);

    }

    public void saveSuccessfulAuth(UserModel userModel, User user, String realm, String client, int typeId) {
        ClientsForMonitoringEntity clientsForMonitoringEntity = clientsForMonitoringService.findAndReturnClientsForMonitoringEntity(realm, client);
        log.info("User is : " + user + ", " + "realm is : " + realm + ", " + "client is : " + client + ", " + "typeId is : " + typeId);
        if (clientsForMonitoringEntity == null || !clientsForMonitoringEntity.isMonitoring()) {
            log.info(" saveSuccessfulAuth is stop, because clientsForMonitoringEntity == null || !clientsForMonitoringEntity.isMonitoring()");
            return;
        }

        UserEntity userEntity = userRepository.findUser(user.getId());
        AuthOrRegTypeEntity authOrRegTypeEntity = authOrRegTypeService.findAndReturn(typeId);
        AuthorisedUsersEntity authorisedUsersEntity = new AuthorisedUsersEntity();
        authorisedUsersEntity.setUser(userEntity);
        authorisedUsersEntity.setAuthType(authOrRegTypeEntity);
        authorisedUsersEntity.setId(UUID.randomUUID().toString());
        authorisedUsersEntity.setRealm(realm);
        authorisedUsersEntity.setAuthorised(LocalDateTime.now());
        authorisedUsersEntity.setClient(clientsForMonitoringEntity);
        authorisedUsersRepository.save(authorisedUsersEntity);

        setParam(userModel, client);
    }

    public void saveSuccessfulAuthFromEventListener(String userId, String realm, String client, int typeId) {
        ClientsForMonitoringEntity clientsForMonitoringEntity = clientsForMonitoringService.findAndReturnClientsForMonitoringEntity(realm, client);
        if (clientsForMonitoringEntity == null || !clientsForMonitoringEntity.isMonitoring()) {
            log.info(" saveSuccessfulAuth is stop, because clientsForMonitoringEntity == null || !clientsForMonitoringEntity.isMonitoring()");
            return;
        }

        UserEntity userEntity = userRepository.findUser(userId);
        AuthOrRegTypeEntity authOrRegTypeEntity = authOrRegTypeService.findAndReturn(typeId);
        AuthorisedUsersEntity authorisedUsersEntity = new AuthorisedUsersEntity();
        authorisedUsersEntity.setUser(userEntity);
        authorisedUsersEntity.setAuthType(authOrRegTypeEntity);
        authorisedUsersEntity.setId(UUID.randomUUID().toString());
        authorisedUsersEntity.setRealm(realm);
        authorisedUsersEntity.setAuthorised(LocalDateTime.now());
        authorisedUsersEntity.setClient(clientsForMonitoringEntity);
        authorisedUsersRepository.save(authorisedUsersEntity);
    }

    private void setParam (UserModel userModel, String client){
        userModel.setSingleAttribute("login_first", "true");
        log.info("set first param for clientId = {}", client);
    }
}
