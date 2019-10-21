package ru.alamics.sso.report;

import ru.alamics.sso.keycloak.repository.ImportUserHistoryRepository;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class ImportReportService {
    @EJB
    private ImportUserHistoryRepository importUserHistoryRepository;
}
