package ru.alamics.sso.keycloak.social;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.broker.IdpReviewProfileAuthenticatorFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import ru.alamics.sso.keycloak.lookup.Lookup;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomIdpReviewProfileAuthenticatorFactory extends IdpReviewProfileAuthenticatorFactory {
    private static final String PROVIDER_ID = "custom-idp-review-profile";
    private static final String DISPLAY_NAME = "Review Profile (custom)";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(UPDATE_PROFILE_ON_FIRST_LOGIN);
        property.setLabel("{{:: 'update-profile-on-first-login' | translate}}");
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setOptions(Collections.singletonList(IdentityProviderRepresentation.UPFLM_ON));
        property.setDefaultValue(IdentityProviderRepresentation.UPFLM_ON);
        property.setHelpText("Define conditions under which a user has to review and update his profile after first-time login. Value 'On' means that"
                + " page for reviewing profile will be displayed and user can review and update his profile. Value 'off' means that page won't be displayed."
                + " Value 'missing' means that page is displayed just when some required attribute is missing (wasn't downloaded from identity provider). Value 'missing' is the default one."
                + " WARN: In case that user clicks 'Review profile info' on link duplications page, the update page will be always displayed. You would need to disable this authenticator to never display the page.");

        configProperties.add(property);
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new CustomIdpReviewProfileAuthenticator(
                Lookup.lookup(SSLContext.class, "tbapiRegistration"),
                Lookup.lookup(SSLContext.class, "tbapiCustomer")
        );
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
    }
}
