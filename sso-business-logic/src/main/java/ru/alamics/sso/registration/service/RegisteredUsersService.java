package ru.alamics.sso.registration.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.auth_n_regi.ClientsForMonitoringService;
import ru.alamics.sso.jpa.entity.auth_reg.AuthOrRegTypeEntity;
import ru.alamics.sso.jpa.entity.auth_reg.ClientsForMonitoringEntity;
import ru.alamics.sso.jpa.entity.auth_reg.RegisteredUsersEntity;
import ru.alamics.sso.jpa.repository.RegisteredUsersRepository;
import ru.alamics.sso.jpa.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
@Named("RegisteredUsersService")
@Slf4j
public class RegisteredUsersService {
    @Inject
    RegisteredUsersRepository registeredUsersRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    AuthOrRegTypeService authOrRegTypeService;

    @Inject
    ClientsForMonitoringService clientsForMonitoringService;

    public void saveSuccessfulReg(String user, String realm, String client, int typeId) {
        log.info(" Client is : " + client);
        log.info(" typeId is : " + typeId);
        ClientsForMonitoringEntity clientsForMonitoringEntity = clientsForMonitoringService.findAndReturnClientsForMonitoringEntity(realm, client);

        if (clientsForMonitoringEntity == null || !clientsForMonitoringEntity.isMonitoring()) {
            return;
        }


        UserEntity userEntity = userRepository.findUser(user);
        AuthOrRegTypeEntity authOrRegTypeEntity = authOrRegTypeService.findAndReturn(typeId);
        RegisteredUsersEntity registeredUsersEntity = new RegisteredUsersEntity();

        registeredUsersEntity.setUser(userEntity);
        registeredUsersEntity.setRegType(authOrRegTypeEntity);
        registeredUsersEntity.setId(UUID.randomUUID().toString());
        registeredUsersEntity.setRealm(realm);
        registeredUsersEntity.setRegistered(LocalDateTime.now());
        registeredUsersEntity.setClient(clientsForMonitoringEntity);
        registeredUsersRepository.save(registeredUsersEntity);
    }
}


