package ru.alamics.sso.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.ClientModel;
import org.keycloak.models.jpa.entities.ClientEntity;
import ru.alamics.sso.jpa.entity.MainRedirectUri;
import ru.alamics.sso.jpa.repository.ClientRepository;
import ru.alamics.sso.util.Util;

import java.util.Set;

@ApplicationScoped
@Slf4j
public class ClientService {
    @Inject
    ClientRepository repository;

    public String getMainRedirectUri(String clientId) {
        MainRedirectUri uri = repository.findMainRedirectUriByClientId(clientId);
        return uri == null ? null : uri.getUri();
    }

    public String findMainRedirectUri(ClientModel client) {

        if (client == null) return null;

        String mru = getMainRedirectUri(client.getId());

        if (mru == null) {

            mru = findRedirectUri(client.getRedirectUris());
        }

        return mru;
    }

    public String findMainRedirectUri(ClientEntity client) {

        if (client == null) return null;

        String mru = getMainRedirectUri(client.getId());

        if (mru == null) {

            mru = findRedirectUri(client.getRedirectUris());
        }

        return mru;
    }

    private String findRedirectUri(Set<String> redirectUris) {

        String res = null;

        for (String rediUrl : redirectUris) {

            if (rediUrl != null) {

                rediUrl = rediUrl.replaceAll("\\*", "");

                if (rediUrl.endsWith("/")) {
                    rediUrl = rediUrl.substring(0, rediUrl.length() - 1);
                }

                if (!Util.isEmpty(rediUrl)) {
                    res = rediUrl;
                    break;
                }
            }
        }

        return res;
    }

    public void saveMainRedirectUri(String clientId, String uri) {
        MainRedirectUri mainRedirectUri = new MainRedirectUri();
        mainRedirectUri.setClientId(clientId);
        mainRedirectUri.setUri(uri);

        repository.saveMainRedirectUri(mainRedirectUri);
    }

}
