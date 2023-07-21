package ru.alamics.sso.registration.service;

import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.auth_n_regi.ClientsForMonitoringService;
import ru.alamics.sso.jpa.entity.auth_reg.AuthOrRegTypeEntity;
import ru.alamics.sso.jpa.entity.auth_reg.ClientsForMonitoringEntity;
import ru.alamics.sso.jpa.entity.auth_reg.RegisteredUsersEntity;
import ru.alamics.sso.jpa.repository.RegisteredUsersRepository;
import ru.alamics.sso.jpa.repository.UserRepository;
import ru.alamics.sso.keycloak.lookup.Lookup;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.time.LocalDateTime;
import java.util.UUID;

@Stateless(name = "RegisteredUsersService")
@LocalBean
public class RegisteredUsersService {

    @EJB
    private final RegisteredUsersRepository registeredUsersRepository;
    @EJB
    private final UserRepository userRepository;

    private final AuthOrRegTypeService authOrRegTypeService;

    private final ClientsForMonitoringService clientsForMonitoringService;

    public RegisteredUsersService() {
        this.registeredUsersRepository = Lookup.lookup(RegisteredUsersRepository.class);
        this.userRepository = Lookup.lookup(UserRepository.class);
        this.authOrRegTypeService = Lookup.lookup(AuthOrRegTypeService.class);
        this.clientsForMonitoringService = Lookup.lookup(ClientsForMonitoringService.class);

    }

    public void saveSuccessfulReg(String user, String realm, String client, int typeId) {

        ClientsForMonitoringEntity clientsForMonitoringEntity = clientsForMonitoringService.findAndReturnClientsForMonitoringEntity(realm, client);

        if (!clientsForMonitoringEntity.isMonitoring()) {
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


