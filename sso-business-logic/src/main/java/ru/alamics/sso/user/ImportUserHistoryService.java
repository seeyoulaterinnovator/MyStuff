package ru.alamics.sso.user;

import ru.alamics.sso.keycloak.repository.ImportUserHistoryRepository;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class ImportUserHistoryService {
    @EJB
    private ImportUserHistoryRepository importUserHistoryRepository;
}
