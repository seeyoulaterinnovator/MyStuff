package ru.alamics.sso.client;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.MainRedirectUri;
import ru.alamics.sso.jpa.repository.ClientRepository;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Stateless
public class ClientService {

    @EJB
    private ClientRepository repository;

    public String findMainRedirectUri(String clientId) {
        MainRedirectUri uri = repository.findMainRedirectUriByClientId(clientId);
        return uri == null ? null : uri.getUri();
    }

    public void saveMainRedirect(String clientId, String uri) {
        MainRedirectUri mainRedirectUri = new MainRedirectUri();
        mainRedirectUri.setClientId(clientId);
        mainRedirectUri.setUri(uri);

        repository.save(mainRedirectUri);
    }

}
