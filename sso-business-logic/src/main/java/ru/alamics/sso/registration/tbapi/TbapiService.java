package ru.alamics.sso.registration.tbapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.registration.tbapi.exception.TbapiRegisterException;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.registration.tbapi.model.TbapiRequest;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.tbapi.port.TbapiRemoteService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.alamics.sso.registration.model.UserConstants.*;

@Slf4j
public class TbapiService {

    // jackson serialize
    ObjectMapper jacksonMapper = new ObjectMapper();

    private final TbapiRemoteService remoteService;

    public TbapiService(TbapiRemoteService remoteService) {
        this.remoteService = remoteService;
    }

    public Map<String, Object> registerUser(User user, TbapiConnectConfig connectConfig) throws TbapiRegisterException
    {

        TbapiRequest request = new TbapiRequest();
        //.id(user.getId())
        request.setEmail(user.getEmail());
        request.setPhoneNumber(user.getPhone());

        List<String> orgg = user.getAttributes().get(ATTR_ORG_NAME);// TODO
        String orgName = orgg == null ? null : orgg.get(0);
        request.setLegalName(orgName);
        request.setName(orgName);

        try {
            String attrStr = jacksonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
            log.info(String.format("Send request to TBAPI: %s", attrStr));
        } catch (Exception e) {
            log.error("", e);
        }

        Map<String, Object> result = remoteService.createCustomer(request, connectConfig);

        try {
            String attrStr = jacksonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            log.info(String.format("Got answer from TBAPI: %s", attrStr));
        } catch (Exception e) {
            log.error("", e);
        }

        // TODO check response

        return filterRegisterResponse(result);
    }

    private Map<String, Object> filterRegisterResponse(Map<String, Object> resp) {

        Map<String, Object> ret = new HashMap<>();

        ret.put(ATTR_TOMS_NAME, resp.get("id"));
        ret.put(ATTR_DMP_NAME, resp.get("dmpCustomerId"));

        return ret;
    }

}
