package ru.alamics.sso.client;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.ClientModel;
import ru.alamics.sso.jpa.entity.MainRedirectUri;
import ru.alamics.sso.jpa.repository.ClientRepository;
import ru.alamics.sso.util.Util;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Slf4j
@Stateless
public class ClientService {

    @EJB
    private ClientRepository repository;

    public String getMainRedirectUri(String clientId) {
        MainRedirectUri uri = repository.findMainRedirectUriByClientId(clientId);
        return uri == null ? null : uri.getUri();
    }

    public String findMainRedirectUri(ClientModel client) {

        String mru = getMainRedirectUri(client.getId());

        if (mru == null) {

            for (String rediUrl : client.getRedirectUris()) {

                if (rediUrl != null) {

                    rediUrl = rediUrl.replaceAll("\\*", "");

                    if (rediUrl.endsWith("/")) {
                        rediUrl = rediUrl.substring(0, rediUrl.length() - 1);
                    }

                    if (!Util.isEmpty(rediUrl)) {
                        mru = rediUrl;
                        break;
                    }
                }
            }
        }

        return mru;
    }

    public void saveMainRedirectUri(String clientId, String uri) {
        MainRedirectUri mainRedirectUri = new MainRedirectUri();
        mainRedirectUri.setClientId(clientId);
        mainRedirectUri.setUri(uri);

        repository.saveMainRedirectUri(mainRedirectUri);
    }

}
