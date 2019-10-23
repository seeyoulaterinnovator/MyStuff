package ru.alamics.sso.user;

import ru.alamics.sso.keycloak.entity.ImportUserHistoryEntity;
import ru.alamics.sso.keycloak.repository.ImportUserHistoryRepository;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.web.ImportUserHistoryDto;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.List;

@Stateless
@LocalBean
public class ImportUserHistoryService {
    @EJB
    private ImportUserHistoryRepository importUserHistoryRepository;

    public void saveImportUserHistory(ImportUserHistoryEntity importUserHistoryEntity) {
        if (importUserHistoryEntity == null) {
            return;
        }
        importUserHistoryRepository.saveImportUserHistory(importUserHistoryEntity);
    }

    public List<ImportUserHistoryDto> getImportUserHistories(String realmId) {
        return UserMapper.toImportUserHistoryDtos(importUserHistoryRepository.findAllImportUserHistoryEntities(realmId));
    }

    public ImportUserHistoryEntity getImportUserHistory(String importId) {
        return importUserHistoryRepository.findImportUserHistory(importId);
    }
}
