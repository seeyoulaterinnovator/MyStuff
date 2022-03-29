/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.service.UserPostService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mappings UserModel property (the property name of a getter method) to an ID Token claim.  Token claim name can be a full qualified nested object name,
 * i.e. "address.country".  This will create a nested
 * json object within the toke claim.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Slf4j
public class UserPostMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {
    private static final String PROVIDER_ID = "user-post-mapper";
    private static final String DISPLAY_NAME = "User Post";
    private static final String HELP_TEXT = "Map a built in user property (email, firstName, lastName) to a token claim.";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ProtocolMapperUtils.USER_ATTRIBUTE);
        property.setLabel(ProtocolMapperUtils.USER_MODEL_PROPERTY_LABEL);
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setOptions(Arrays.stream(UserPostPropertyType.values()).map(Enum::toString).collect(Collectors.toList()));
        property.setHelpText(ProtocolMapperUtils.USER_MODEL_PROPERTY_HELP_TEXT);
        configProperties.add(property);
        ProviderConfigProperty multiValued = new ProviderConfigProperty();
        multiValued.setName(ProtocolMapperUtils.MULTIVALUED);
        multiValued.setLabel(ProtocolMapperUtils.MULTIVALUED_LABEL);
        multiValued.setHelpText(ProtocolMapperUtils.MULTIVALUED_HELP_TEXT);
        multiValued.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        configProperties.add(multiValued);
        OIDCAttributeMapperHelper.addAttributeConfig(configProperties, UserPropertyMapper.class);
    }

    private UserPostService userPostService;

    public static ProtocolMapperModel createClaimMapper(String name,
                                                        String userAttribute,
                                                        String tokenClaimName, String claimType,
                                                        boolean accessToken, boolean idToken) {
        return OIDCAttributeMapperHelper.createClaimMapper(name, userAttribute,
                tokenClaimName, claimType,
                accessToken, idToken,
                PROVIDER_ID);
    }

    public static Object getUserModelValue(UserPostResponse userPost, String propertyName) {
        switch (UserPostPropertyType.valueOf(propertyName)) {
            case POST_ID:
                return userPost.getId();
            case TOMS_ID:
                return userPost.getTomsId();
            case DMP_ID:
                return userPost.getDmpId();
            case ROLE:
                return userPost.getUserRole() == null ? "" : userPost.getUserRole().getName();
            case SYSTEMS:
                if (userPost.getSystemRoles() == null)
                    return Collections.EMPTY_LIST;

                return userPost.getSystemRoles().stream()
                        .filter(o -> o != null && o.getExternalSystem() != null)
                        .map(o -> o.getExternalSystem().getName())
                        .collect(Collectors.toList());
        }
        return "";
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
        String propertyName = mappingModel.getConfig().get(ProtocolMapperUtils.USER_ATTRIBUTE);
        UserPostResponse userPost = getUserPost(user);
        if (propertyName == null || propertyName.trim().isEmpty() || userPost == null) return;

        Object propertyValue = getUserModelValue(userPost, propertyName);
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, propertyValue);
    }

    private UserPostResponse getUserPost(UserModel user) {
        this.userPostService = Lookup.lookup(UserPostService.class);
        return userPostService.getUserPost(user.getId()).stream().filter(o -> o.isSelected()).findFirst()
                .orElse(null);
    }
}
