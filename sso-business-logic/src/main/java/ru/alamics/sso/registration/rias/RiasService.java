package ru.alamics.sso.registration.rias;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.rias.exception.RiasCheckException;
import ru.alamics.sso.registration.rias.model.RiasLogin;
import ru.alamics.sso.registration.rias.port.RiasApiService;
import ru.alamics.sso.registration.rias.port.RiasLoginService;
import ru.alamics.sso.util.Util;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Slf4j
@Stateless
public class RiasService {

    @EJB
    private RiasApiService riasApiService;

    @EJB
    private RiasLoginService riasLoginService;

    public RiasService(RiasApiService riasApiService) {
        this.riasApiService = riasApiService;
    }

    public RiasService() {

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

    public RiasLogin loginUser(String login, String password) {

        try {
            return riasLoginService.loginUser(login, password);

        } catch (RiasCheckException e) {
            log.error("RIAS login service", e);
        }

        return null;
    }

}
