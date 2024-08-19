package ru.alamics.sso.antifraud;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.antifraud.WroteCodeAttemptsEntity;
import ru.alamics.sso.jpa.repository.UserRepository;
import ru.alamics.sso.jpa.repository.WroteCodeAttemptsRepository;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class WroteCodeAttemptsService {
    @Inject
    private WroteCodeAttemptsRepository wroteCodeAttemptsRepository;

    @Inject
    private UserRepository userRepository;

    public WroteCodeAttemptsService() {
        this.wroteCodeAttemptsRepository = Lookup.lookup(WroteCodeAttemptsRepository.class);
        this.userRepository = Lookup.lookup(UserRepository.class);
    }

    public int getWroteCodeAttemptsByCode(String phone, String realm, String type, String code){
        return wroteCodeAttemptsRepository.getWroteCodeAttemptsByCode(phone, realm, type, code).size();
    }

    public void saveFailWroteCode(String code, String userCode, String phone, String realm, String typeSend, User user){
        UserEntity userEntity = userRepository.findUser(user.getId());
        WroteCodeAttemptsEntity wroteCodeAttemptsEntity = new WroteCodeAttemptsEntity();
        wroteCodeAttemptsEntity.setId(UUID.randomUUID().toString());
        wroteCodeAttemptsEntity.setCode(code);
        wroteCodeAttemptsEntity.setRealm(realm);
        wroteCodeAttemptsEntity.setPhone(phone);
        wroteCodeAttemptsEntity.setTypeSend(typeSend);
        wroteCodeAttemptsEntity.setCreated(LocalDateTime.now());
        wroteCodeAttemptsEntity.setUserCode(userCode);
        wroteCodeAttemptsEntity.setUser(userEntity);
        wroteCodeAttemptsRepository.save(wroteCodeAttemptsEntity);
    }
}
