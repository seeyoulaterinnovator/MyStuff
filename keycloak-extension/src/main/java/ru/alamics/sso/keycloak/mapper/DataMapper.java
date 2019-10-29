package ru.alamics.sso.keycloak.mapper;

import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.model.TbapiConstants;
import ru.alamics.sso.registration.tbapi.TbapiService;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.remote.tbapi.TbapiServiceRestImpl;
import ru.alamics.sso.user.web.UserSearchDto;

import java.util.List;
import java.util.Map;

public abstract class DataMapper {

    private static TbapiService tbapiService = new TbapiService(new TbapiServiceRestImpl());

    public static List<UserPostResponse> getUserPostResponsesWithOrganizations(List<UserPostResponse> userPostResponses) {
        Map<String, Object> customerNames = tbapiService.customerNames(connectConfig(),
                userPostResponses.stream()
                        .filter(o -> o.getTomsId() != null)
                        .map(UserPostResponse::getTomsId).toArray(String[]::new));
        userPostResponses.stream()
                .filter(o -> o.getTomsId() != null)
                .forEach(o -> o.setOrganization((String) customerNames.get(o.getTomsId())));
        return userPostResponses;
    }

    private static TbapiConnectConfig connectConfig() {
        ApplicationProperties properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);
        TbapiConnectConfig connectConfig = new TbapiConnectConfig();
        if (properties != null) {
            connectConfig.setHost(properties.getProperty(TbapiConstants.HOST));
            connectConfig.setPort(Integer.parseInt(properties.getProperty(TbapiConstants.PORT)));
            connectConfig.setAppname(properties.getProperty(TbapiConstants.AUTH_APPNAME));
            connectConfig.setUsername(properties.getProperty(TbapiConstants.AUTH_USERNAME));
            connectConfig.setPath(properties.getProperty(TbapiConstants.CUSTOMER_FIND_PATH));
            connectConfig.setSecure(Boolean.parseBoolean(TbapiConstants.SECURE));
        }

        return connectConfig;
    }

    public static List<UserSearchDto> addOrganizationToUserSearchDtos(List<UserSearchDto> userSearchDtos) {
        if (userSearchDtos == null || userSearchDtos.isEmpty()) {
            return null;
        }

        tbapiService = new TbapiService(new TbapiServiceRestImpl());
        Map<String, Object> customerNames = tbapiService.customerNames(connectConfig(),
                userSearchDtos.stream()
                        .filter(o -> o.getTomsId() != null)
                        .map(UserSearchDto::getTomsId).toArray(String[]::new));
        userSearchDtos.stream()
                .filter(o -> o.getTomsId() != null)
                .forEach(o -> o.setOrganization((String) customerNames.get(o.getTomsId())));
        return userSearchDtos;
    }
}
