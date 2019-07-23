package ru.alamics.sso.registration.rias;

import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.rias.port.RiasApiService;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class RiasService {

    @EJB
    private RiasApiService riasApiService;

    public RiasService(RiasApiService riasApiService) {
        this.riasApiService = riasApiService;
    }

    public RiasService() {

    }

    public boolean checkEmail(User user) {

        String email = user.getEmail();

        return riasApiService.checkParam(email);
    }

    public boolean checkPhone(User user) {

        String phone = user.getPhone();

        return riasApiService.checkParam(phone);
    }

}
