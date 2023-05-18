package ru.alamics.sso.antifraud;

import ru.alamics.sso.jpa.entity.UserLoginHistory;
import ru.alamics.sso.jpa.entity.antifraud.AttemptFailsEntity;
import ru.alamics.sso.jpa.repository.AttemptFailsRepository;
import ru.alamics.sso.jpa.repository.BlackListRepository;
import ru.alamics.sso.jpa.repository.UserHistoryLoginRepository;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.util.AttemptFailsMapper;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
@LocalBean
public class AttemptFailsService {
    @EJB
    private final AttemptFailsRepository repository;
    @EJB
    private final BlackListRepository blackListRepository;
    @EJB
    private final UserHistoryLoginRepository userHistoryLoginRepository;

    public AttemptFailsService() {
        this.repository = Lookup.lookup(AttemptFailsRepository.class);
        this.blackListRepository = Lookup.lookup(BlackListRepository.class);
        this.userHistoryLoginRepository = Lookup.lookup(UserHistoryLoginRepository.class);
    }

    public void saveAttempt(AttemptFailsDto dto) {
        repository.save(AttemptFailsMapper.toEntity(dto));
    }

    public List<AttemptFailsDto> getAttempts(String phone, String realm, String cause, String userId) {
        if (isCountFromLastAuth(phone, realm, cause, userId)) {
             return AttemptFailsMapper.toDtoList(repository.getFailAttemptsIfAuthSuccess(phone, realm, cause));
        }
        List<AttemptFailsEntity> entities = repository.getFailAttemptsIfWasBlocked(phone, realm, cause);
        if (entities.isEmpty() && !blackListRepository.isWasBlockedByPhoneRealmCause(phone, realm, cause)) {
            return AttemptFailsMapper.toDtoList(repository.getFailAttemptsByPhoneAndRealm(phone, realm, cause));
        }
        return AttemptFailsMapper.toDtoList(entities) ;
    }
    //fixme pofixit govnocod
    private boolean isCountFromLastAuth(String phone, String realm, String cause, String userId) {
        if (!userHistoryLoginRepository.findLastAuthSuccess(userId, realm).isEmpty()) {
            LocalDateTime lastLogin = userHistoryLoginRepository.findLastAuthSuccess(userId, realm).stream()
                    .findFirst().get().getLoginedAt();
            if (blackListRepository.isWasBlockedByPhoneRealmCause(phone, realm, cause)) {
                LocalDateTime lastBlock = blackListRepository.findBlockedByPhoneRealmCause(phone, realm, cause).stream()
                        .findFirst().get().getCreatedAt();
                return lastLogin.isAfter(lastBlock);
            }
        }
        return false;
    }

    public void deleteAttempts(List<AttemptFailsDto> dto) {
        repository.delete(AttemptFailsMapper.toEntityList(dto));
    }
}
