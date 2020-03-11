package ru.alamics.sso.registration.tbapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.tbapi.exception.TbapiRegisterException;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.registration.tbapi.model.TbapiRequest;
import ru.alamics.sso.registration.tbapi.model.TbapiResponse;
import ru.alamics.sso.registration.tbapi.port.TbapiRemoteService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.alamics.sso.registration.model.UserConstants.*;

@Slf4j
public class TbapiService {

    private final static String TBAPI_TOMS_ID = "id";
    private final static String TBAPI_DMP_ID = "dmpCustomerId";

    private final static String TBAPI_ERROR_FLAG = "businessErrorCode";
    private final static String TBAPI_ERROR_DETAIL = "userMessage";

    // jackson serialize
    ObjectMapper jacksonMapper = new ObjectMapper();

    private final TbapiRemoteService remoteService;

    public TbapiService(TbapiRemoteService remoteService) {
        this.remoteService = remoteService;
    }

    public Map<String, Object> registerUser(User user, TbapiConnectConfig connectConfig) throws TbapiRegisterException {

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

        TbapiResponse result = remoteService.createCustomer(request, connectConfig);

        try {
            String attrStr = jacksonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            log.info(String.format("Got answer from TBAPI: %s", attrStr));
        } catch (Exception e) {
            log.error("", e);
        }

        checkResponse(result);

        return filterRegisterResponse(result);
    }

    private void checkResponse(TbapiResponse resp) throws TbapiRegisterException {
        if (resp.getBusinessErrorCode() != null)
            throw new TbapiRegisterException(resp.getUserMessage());

        if (resp.getId() == null)
            throw new TbapiRegisterException("TOMS ID not found");
    }

    private Map<String, Object> filterRegisterResponse(TbapiResponse resp) {

        Map<String, Object> ret = new HashMap<>();

        ret.put(ATTR_TOMS_NAME, resp.getId());

        if (resp.getExtendedMap() != null &&
                resp.getExtendedMap().getCustomerHolder() != null &&
                resp.getExtendedMap().getCustomerHolder().getSingleValue() != null)
            ret.put(ATTR_DMP_NAME, resp.getExtendedMap().getCustomerHolder().getSingleValue().getAttributeValue());

        return ret;
    }

    public Map<String, Object> customerNames(TbapiConnectConfig connectConfig, List<String> customerIds) {
        return remoteService.getCustomerName(customerIds, connectConfig);
    }
}
