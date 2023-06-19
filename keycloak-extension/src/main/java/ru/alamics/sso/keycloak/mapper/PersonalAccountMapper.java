package ru.alamics.sso.keycloak.mapper;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.user.PersonalAccountService;
import ru.alamics.sso.user.model.PersonalAccountPostModel;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PersonalAccountMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final String POST_PERSONAL_ACCOUNT = "post.personal.account";

    private static final String PROVIDER_ID = "personal-account-mapper";
    private static final String DISPLAY_NAME = "Personal Account";
    private static final String HELP_TEXT = "Map a personal account list of user post to a token claim.";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty multiValued = new ProviderConfigProperty();
        multiValued.setName(ProtocolMapperUtils.MULTIVALUED);
        multiValued.setLabel(ProtocolMapperUtils.MULTIVALUED_LABEL);
        multiValued.setHelpText(ProtocolMapperUtils.MULTIVALUED_HELP_TEXT);
        multiValued.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        configProperties.add(multiValued);

        //OIDCAttributeMapperHelper.addAttributeConfig(configProperties, PersonalAccountMapper.class);

        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        addJsonTypeConfig(configProperties);

        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, PersonalAccountMapper.class);
    }

    private UserPostService userPostService;
    private PersonalAccountService paService;

    public static void addJsonTypeConfig(List<ProviderConfigProperty> configProperties) {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(OIDCAttributeMapperHelper.JSON_TYPE);
        property.setLabel(OIDCAttributeMapperHelper.JSON_TYPE);
        List<String> types = new ArrayList<>();
        //types.add("String");
        types.add("JSON");
        //types.add("long");
        //types.add("int");
        //types.add("boolean");
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setOptions(types);
        property.setHelpText(OIDCAttributeMapperHelper.JSON_TYPE_TOOLTIP);
        configProperties.add(property);
    }

    public static ProtocolMapperModel createClaimMapper(String name,
                                                        String userAttribute,
                                                        String tokenClaimName, String claimType,
                                                        boolean accessToken, boolean idToken) {
        return OIDCAttributeMapperHelper.createClaimMapper(name, userAttribute,
                tokenClaimName, claimType,
                accessToken, idToken,
                PROVIDER_ID);
    }

    public static Object getModelValue(PersonalAccountPostModel accountModel) {
        return accountModel;
    }

    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {

        UserModel user = userSession.getUser();

        PersonalAccountPostModel accountModel = getUserPost(user);
        if (accountModel == null) return;

        Object propertyValue = getModelValue(accountModel);
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, propertyValue);
    }

    private PersonalAccountPostModel getUserPost(UserModel user) {

        this.paService = Lookup.lookup(PersonalAccountService.class);
        return paService.getActivePAByUser(user.getId());

    }

}
