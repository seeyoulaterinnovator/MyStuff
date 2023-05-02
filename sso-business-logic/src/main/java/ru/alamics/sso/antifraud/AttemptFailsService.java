package ru.alamics.sso.antifraud;

import lombok.NoArgsConstructor;
import ru.alamics.sso.jpa.repository.AttemptFailsRepository;
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

    public AttemptFailsService() {
        this.repository = Lookup.lookup(AttemptFailsRepository.class);
    }

    public void saveAttempt(AttemptFailsDto dto) {
        repository.save(AttemptFailsMapper.toEntity(dto));
    }

    public List<AttemptFailsDto> getAttempts(String phone, String realm, String cause) {
        return AttemptFailsMapper.toDtoList(repository.getFailAttemptsByPhoneAndRealm(phone, realm, cause));
    }

    public void deleteAttempts(List<AttemptFailsDto> dto) {
        repository.delete(AttemptFailsMapper.toEntityList(dto));
    }
}
