package ru.alamics.sso.registration.rias;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.rias.exception.RiasCheckException;
import ru.alamics.sso.registration.rias.model.RiasLogin;
import ru.alamics.sso.registration.rias.port.RiasApiService;
import ru.alamics.sso.registration.rias.port.RiasLoginService;
import ru.alamics.sso.util.Util;

@ApplicationScoped
@Slf4j
public class RiasService {
    final RiasApiService riasApiService;

    final RiasLoginService riasLoginService;

    @Inject
    public RiasService(
            RiasApiService riasApiService,
            RiasLoginService riasLoginService
    ) {
        this.riasApiService = riasApiService;
        this.riasLoginService = riasLoginService;
    }

    public boolean checkEmail(User user) {

        String email = user.getEmail();
        if (Util.isEmpty(email))
            return false;

        try {
            return riasApiService.checkParam(email);

        } catch (RiasCheckException rce) {
            log.error("RIAS check service", rce);
        }

        return false;
    }

    public boolean checkPhone(User user) {

        String phone = user.getPhone();
        if (Util.isEmpty(phone))
            return false;

        try {
            return riasApiService.checkParam(Util.getCleanUserPhone(phone));

        } catch (RiasCheckException rce) {
            log.error("RIAS check service", rce);
        }

        return false;
    }

    public RiasLogin loginUser(String domain, String login, String password) {

        try {
            return riasLoginService.loginUser(domain, login, password);

        } catch (RiasCheckException e) {
            log.error("RIAS login service", e);
        }

        return null;
    }

}
