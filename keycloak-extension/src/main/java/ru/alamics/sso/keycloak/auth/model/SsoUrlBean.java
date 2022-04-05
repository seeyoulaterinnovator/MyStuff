package ru.alamics.sso.keycloak.auth.model;

import org.keycloak.forms.login.freemarker.model.UrlBean;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.services.resources.RealmsResource;
import org.keycloak.theme.Theme;
import ru.alamics.sso.util.Util;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static ru.alamics.sso.registration.model.UserConstants.I_FRAME;

public class SsoUrlBean extends UrlBean {
    private final URI baseURI;
    private final RealmModel realm;
    private final boolean isFrame;

    public SsoUrlBean(RealmModel realm, Theme theme, URI baseURI, URI actionUri, boolean isFrame) {
        super(realm, theme, baseURI, actionUri);
        this.baseURI = baseURI;
        this.realm = realm;
        this.isFrame = isFrame;
    }

    @Override
    public String getRegistrationUrl() {
        UriBuilder builder = UriBuilder.fromUri(baseURI)
                .path(RealmsResource.class)
                .path(RealmsResource.class, "getLoginActionsService")
                .path(LoginActionsService.class, "registerPage");

        if (isFrame) {
            //Установка отметки, что вызов происходит в iframe. Необходимо при переходах между вкладками логина и регистрации внутри iframe
            builder.queryParam(I_FRAME, Util.TRUE_STR);
        }

        return builder.build(realm.getName())
                .toString();
    }

    @Override
    public String getLoginUrl() {
        UriBuilder builder = UriBuilder.fromUri(baseURI)
                .path(RealmsResource.class)
                .path(RealmsResource.class, "getLoginActionsService")
                .path(LoginActionsService.class, "authenticate");

        if (isFrame) {
            builder.queryParam(I_FRAME, Util.TRUE_STR);
        }

        return builder.build(realm.getName())
                .toString();
    }
}
