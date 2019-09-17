package ru.alamics.sso.registration.rias.port;

import ru.alamics.sso.registration.rias.exception.RiasCheckException;

public interface RiasApiService {

    boolean checkParam(String param) throws RiasCheckException;

}
