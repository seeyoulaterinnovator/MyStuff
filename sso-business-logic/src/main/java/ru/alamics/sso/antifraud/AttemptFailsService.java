package ru.alamics.sso.antifraud;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.UserLoginHistory;
import ru.alamics.sso.jpa.entity.antifraud.AttemptFailsEntity;
import ru.alamics.sso.jpa.repository.AttemptFailsRepository;
import ru.alamics.sso.jpa.repository.BlackListRepository;
import ru.alamics.sso.jpa.repository.UserHistoryLoginRepository;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.util.AttemptFailsMapper;

import java.util.List;

@ApplicationScoped
public class AttemptFailsService {
    @Inject
    private AttemptFailsRepository repository;
    @Inject
    private BlackListRepository blackListRepository;
    @Inject
    private UserHistoryLoginRepository userHistoryLoginRepository;

    public AttemptFailsService() {
        this.repository = Lookup.lookup(AttemptFailsRepository.class);
        this.blackListRepository = Lookup.lookup(BlackListRepository.class);
        this.userHistoryLoginRepository = Lookup.lookup(UserHistoryLoginRepository.class);
    }

    public void saveAttempt(AttemptFailsDto dto) {
        repository.save(AttemptFailsMapper.toEntity(dto));
    }

    public List<AttemptFailsDto> getAttempts(String phone, String realm, String cause, UserEntity user) {
        if (isCountFromLastAuth(phone, realm, cause, user)) {
            return AttemptFailsMapper.toDtoList(repository.getFailAttemptsIfAuthSuccess(phone, realm, cause, user));
        }
        List<AttemptFailsEntity> entities = repository.getFailAttemptsIfWasBlocked(phone, realm, cause);
        if (entities.isEmpty() && !blackListRepository.isWasBlockedByPhoneRealmCause(phone, realm, cause)) {
            return AttemptFailsMapper.toDtoList(repository.getFailAttemptsByPhoneAndRealm(phone, realm, cause));
        }
        return AttemptFailsMapper.toDtoList(entities);
    }

    private boolean isCountFromLastAuth(String phone, String realm, String cause, UserEntity user) {
        List<UserLoginHistory> userLoginHistories = userHistoryLoginRepository.findLastAuthSuccess(user, realm);
        if (userLoginHistories.isEmpty()) {
            return false;
        }
        if (!blackListRepository.isWasBlockedByPhoneRealmCause(phone, realm, cause)) {
            return false;
        }

        for (UserLoginHistory history : userLoginHistories) {
            return history.getLoginedAt().isAfter(blackListRepository.findBlockedByPhoneRealmCause(phone, realm, cause)
                    .stream().findFirst().get().getUnblockedAt());
        }

        return false;
    }

    public void deleteAttempts(List<AttemptFailsDto> dto) {
        repository.delete(AttemptFailsMapper.toEntityList(dto));
    }

    public int getActualAttemptFailsCount(String phone, String realm, String cause, String userId) {
        return repository.getActualAttemptFailsCount(phone, realm, cause, userId);
    }
}
