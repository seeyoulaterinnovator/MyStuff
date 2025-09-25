package ru.alamics.sso.keycloak.brand.mapper;

import org.keycloak.models.*;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.representations.IDToken;
import org.keycloak.provider.ProviderConfigProperty;

import ru.alamics.sso.jpa.entity.BrandEntity;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.service.BrandService;

import java.util.*;

public class BrandInfoProtocolMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static final String PROVIDER_ID = "brand-info-mapper";

    // config keys (на будущее – можно не менять)
    private static final String CFG_ROOT = "rootClaim";
    private static final String CFG_EMIT_ID = "emitBrandId";

    private static final List<ProviderConfigProperty> CONFIG = new ArrayList<>();

    static {
        ProviderConfigProperty root = new ProviderConfigProperty();
        root.setName(CFG_ROOT);
        root.setLabel("Root claim (optional)");
        root.setHelpText("If set (e.g. 'brand'), mapper will put {code,name,id?} inside that object.");
        root.setType(ProviderConfigProperty.STRING_TYPE);
        root.setDefaultValue("");
        CONFIG.add(root);

        ProviderConfigProperty emitId = new ProviderConfigProperty();
        emitId.setName(CFG_EMIT_ID);
        emitId.setLabel("Emit brand id");
        emitId.setHelpText("Also include marketingBrandId (brand id) into the token.");
        emitId.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        emitId.setDefaultValue("true");
        CONFIG.add(emitId);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Brand Info (code & name)";
    }

    @Override
    public String getDisplayCategory() {
        return "Token mapper";
    }

    @Override
    public String getHelpText() {
        return "Adds brandCode and brandName (and optionally marketingBrandId) to tokens based on user markBrandId or realm default brand.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG;
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mapperModel,
                            UserSessionModel userSession, KeycloakSession session,
                            ClientSessionContext clientSessionCtx) {
        RealmModel realm = userSession.getRealm();
        UserModel user = userSession.getUser();

        String brandId = user.getFirstAttribute("markBrandId");

        BrandService service = Lookup.lookup(BrandService.class);
        BrandEntity brand = null;

        if (brandId != null && !brandId.isBlank()) {
            brand = service.getBrandById(brandId).orElse(null);
        }
        if (brand == null) {
            brand = service.getDefaultBrandByRealm(realm.getName()).orElse(null);
        }
        if (brand == null) {
            return;
        }

        boolean emitId = "true".equalsIgnoreCase(mapperModel.getConfig().getOrDefault(CFG_EMIT_ID, "true"));
        String root = mapperModel.getConfig().getOrDefault(CFG_ROOT, "").trim();

        if (!root.isEmpty()) {
            // вложенный объект: "brand": { code, name, (id) }
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("brandCode", brand.getCode());
            obj.put("brandName", brand.getName());
            if (emitId) obj.put("markBrandId", brand.getId());
            OIDCAttributeMapperHelper.mapClaim(token, mapperModel, obj);
        } else {
            // плоские поля
            token.getOtherClaims().put("brandCode", brand.getCode());
            token.getOtherClaims().put("brandName", brand.getName());
            if (emitId) token.getOtherClaims().put("markBrandId", brand.getId());
        }
    }
}