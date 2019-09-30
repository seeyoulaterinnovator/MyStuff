package ru.alamics.sso.registration.rias.port;

import ru.alamics.sso.registration.rias.exception.RiasCheckException;
import ru.alamics.sso.registration.rias.model.RiasLogin;

public interface RiasLoginService {

    RiasLogin loginUser(String username, String password) throws RiasCheckException;
}
