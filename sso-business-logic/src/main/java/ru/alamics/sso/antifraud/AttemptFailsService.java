package ru.alamics.sso.antifraud;

import ru.alamics.sso.jpa.entity.antifraud.AttemptFailsEntity;
import ru.alamics.sso.jpa.repository.AttemptFailsRepository;
import ru.alamics.sso.jpa.repository.BlackListRepository;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.util.AttemptFailsMapper;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.List;

@Stateless
@LocalBean
public class AttemptFailsService {
    @EJB
    private final AttemptFailsRepository repository;
    @EJB
    private final BlackListRepository blackListRepository;

    public AttemptFailsService() {
        this.repository = Lookup.lookup(AttemptFailsRepository.class);
        this.blackListRepository = Lookup.lookup(BlackListRepository.class);
    }

    public void saveAttempt(AttemptFailsDto dto) {
        repository.save(AttemptFailsMapper.toEntity(dto));
    }

    public List<AttemptFailsDto> getAttempts(String phone, String realm, String cause) {
        List<AttemptFailsEntity> entities = repository.getFailAttemptsIfWasBlocked(phone, realm, cause);
        if (entities.isEmpty() && !blackListRepository.isWasBlockedByPhoneRealmCause(phone, realm, cause)) {
            return AttemptFailsMapper.toDtoList(repository.getFailAttemptsByPhoneAndRealm(phone, realm, cause));
        }
        return AttemptFailsMapper.toDtoList(entities) ;
    }

    public void deleteAttempts(List<AttemptFailsDto> dto) {
        repository.delete(AttemptFailsMapper.toEntityList(dto));
    }
}
