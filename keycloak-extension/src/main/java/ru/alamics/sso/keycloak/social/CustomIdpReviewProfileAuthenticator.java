package ru.alamics.sso.keycloak.social;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.IdpReviewProfileAuthenticator;
import org.keycloak.authentication.authenticators.broker.IdpReviewProfileAuthenticatorFactory;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.resources.AttributeFormDataProcessor;
import org.keycloak.services.validation.Validation;
import org.keycloak.util.JsonSerialization;
import ru.alamics.sso.registration.UserExtension;
import ru.alamics.sso.registration.model.FormConstants;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.tbapi.TbapiService;
import ru.alamics.sso.registration.tbapi.exception.TbapiRegisterException;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.remote.tbapi.TbapiServiceRestImpl;
import ru.alamics.sso.util.Util;

import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.keycloak.authentication.forms.RegistrationRecaptcha.G_RECAPTCHA_RESPONSE;
import static ru.alamics.sso.registration.model.UserConstants.ATTR_ORG_NAME;

@Slf4j
public class CustomIdpReviewProfileAuthenticator extends IdpReviewProfileAuthenticator {

    private static final String SITE_KEY = "6LfQG68UAAAAAOowA30NhSf4_VjiuH_KeT8bN3_B";
    private static final String SITE_SECRET_VAL = "6LfQG68UAAAAAH8quIVwZ_8Cizgwi6CqjPIP5a3w";

    private final TbapiService tbapiService;
    private final UserExtension userExtension;

    public CustomIdpReviewProfileAuthenticator() {
        tbapiService = new TbapiService(new TbapiServiceRestImpl());
        userExtension = new UserExtension();

    }

    @Override
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext userCtx, BrokeredIdentityContext brokerContext) {
        IdentityProviderModel idpConfig = brokerContext.getIdpConfig();

        if (requiresUpdateProfilePage(context, userCtx, brokerContext)) {

            log.info("Identity provider '{}' requires update profile action for broker user '{}'.", idpConfig.getAlias(), userCtx.getUsername());
            String userLanguageTag = context.getSession().getContext().resolveLocale(context.getUser()).toLanguageTag();
            var form = context.form()
                    .setAttribute(LoginFormsProvider.UPDATE_PROFILE_CONTEXT_ATTR, userCtx)
                    .setFormData(null);
            form.setAttribute("recaptchaRequired", true);
            form.setAttribute("recaptchaSiteKey", SITE_KEY);
            form.addScript("https://www.google.com/recaptcha/api.js?hl=" + userLanguageTag);
            context.challenge(form.createUpdateProfilePage());
        } else {
            // Not required to update profile. Marked success
            context.success();
        }
    }

    protected boolean requiresUpdateProfilePage(AuthenticationFlowContext context, SerializedBrokeredIdentityContext userCtx, BrokeredIdentityContext brokerContext) {
        String enforceUpdateProfile = context.getAuthenticationSession().getAuthNote(ENFORCE_UPDATE_PROFILE);
        if (Boolean.parseBoolean(enforceUpdateProfile)) {
            return true;
        }

        String updateProfileFirstLogin;
        AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();
        if (authenticatorConfig == null || !authenticatorConfig.getConfig().containsKey(IdpReviewProfileAuthenticatorFactory.UPDATE_PROFILE_ON_FIRST_LOGIN)) {
            updateProfileFirstLogin = IdentityProviderRepresentation.UPFLM_MISSING;
        } else {
            updateProfileFirstLogin = authenticatorConfig.getConfig().get(IdpReviewProfileAuthenticatorFactory.UPDATE_PROFILE_ON_FIRST_LOGIN);
        }

        RealmModel realm = context.getRealm();
        return IdentityProviderRepresentation.UPFLM_ON.equals(updateProfileFirstLogin)
                || (IdentityProviderRepresentation.UPFLM_MISSING.equals(updateProfileFirstLogin) && !Validation.validateUserMandatoryFields(realm, userCtx));
    }

    @Override
    protected void actionImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext userCtx, BrokeredIdentityContext brokerContext) {
        EventBuilder event = context.getEvent();
        event.event(EventType.UPDATE_PROFILE);
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        RealmModel realm = context.getRealm();

        List<FormMessage> errors = getValidationErrorList(context, realm, formData);
        try {
            fillUserContextFromTbApi(formData, userCtx);
        } catch (TbapiRegisterException e) {
            errors.add(new FormMessage("Регистрация временно недоступна, попробуйте повторить попытку позже"));
        }

        if (!errors.isEmpty()) {
            String userLanguageTag = context.getSession().getContext().resolveLocale(context.getUser()).toLanguageTag();
            var form = context.form()
                    .setErrors(errors)
                    .setAttribute(LoginFormsProvider.UPDATE_PROFILE_CONTEXT_ATTR, userCtx)
                    .setAttribute("recaptchaRequired", true)
                    .setAttribute("recaptchaSiteKey", SITE_KEY);
            form.addScript("https://www.google.com/recaptcha/api.js?hl=" + userLanguageTag);
            form.setFormData(formData);

            context.challenge(form.createUpdateProfilePage());
            return;
        }

        String username = realm.isRegistrationEmailAsUsername() ? formData.getFirst(UserModel.EMAIL) : formData.getFirst(UserModel.USERNAME);
        userCtx.setUsername(username);
        userCtx.setFirstName(formData.getFirst(UserModel.FIRST_NAME));
        userCtx.setLastName(formData.getFirst(UserModel.LAST_NAME));

        String phone = Util.getCleanUserPhone(formData.getFirst(FormConstants.FIELD_PHONE));
        if (phone != null)
            userCtx.setAttribute(FormConstants.FIELD_PHONE, Collections.singletonList(phone));
        userCtx.setAttribute(FormConstants.FIELD_ORG_NAME, Collections.singletonList(formData.getFirst(FormConstants.FIELD_ORG_NAME)));

        String email = formData.getFirst(FormConstants.FIELD_EMAIL);
        if (!ObjectUtil.isEqualOrBothNull(email, userCtx.getEmail())) {

            userCtx.setEmail(email);
            context.getAuthenticationSession().setAuthNote(UPDATE_PROFILE_EMAIL_CHANGED, "true");
        }

        AttributeFormDataProcessor.process(formData, realm, userCtx);

        userCtx.saveToAuthenticationSession(context.getAuthenticationSession(), BROKERED_CONTEXT_NOTE);

        log.debug("Profile updated successfully after first authentication with identity provider {} for broker user {}.", brokerContext.getIdpConfig().getAlias(), userCtx.getUsername());

        event.detail(Details.UPDATED_EMAIL, email);
        context.success();
    }

    private List<FormMessage> getValidationErrorList(AuthenticationFlowContext context, RealmModel realm, MultivaluedMap<String, String> formData) {
        List<FormMessage> errors = Validation.validateUpdateProfileForm(realm, formData);
        if (Validation.isBlank(formData.getFirst(FormConstants.FIELD_ORG_NAME))) {
            errors.add(new FormMessage(FormConstants.FIELD_ORG_NAME, "missingOrgNameMessage"));
        }
        if (Validation.isBlank(formData.getFirst(FormConstants.FIELD_PHONE))) {
            errors.add(new FormMessage(FormConstants.FIELD_PHONE, "missingPhoneNumberMessage"));
        }

        String captcha = formData.getFirst(G_RECAPTCHA_RESPONSE);
        if (Validation.isBlank(captcha) || !validateRecaptcha(context, captcha)) {
            errors.add(new FormMessage(null, Messages.RECAPTCHA_FAILED));
            formData.remove(G_RECAPTCHA_RESPONSE);
        }

        return errors;
    }

    private boolean validateRecaptcha(AuthenticationFlowContext context, String captcha) {
        HttpClient httpClient = context.getSession().getProvider(HttpClientProvider.class).getHttpClient();
        HttpPost post = new HttpPost("https://www.google.com/recaptcha/api/siteverify");
        List<NameValuePair> formparams = new LinkedList<>();
        formparams.add(new BasicNameValuePair("secret", SITE_SECRET_VAL));
        formparams.add(new BasicNameValuePair("response", captcha));
        formparams.add(new BasicNameValuePair("remoteip", context.getConnection().getRemoteAddr()));
        try {
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
            post.setEntity(form);
            HttpResponse response = httpClient.execute(post);
            InputStream content = response.getEntity().getContent();
            try {
                Map json = JsonSerialization.readValue(content, Map.class);
                Object val = json.get("success");
                return Boolean.TRUE.equals(val);
            } finally {
                content.close();
            }
        } catch (Exception e) {
            ServicesLogger.LOGGER.recaptchaFailed(e);
        }
        return false;
    }

    private void fillUserContextFromTbApi(MultivaluedMap<String, String> formData, SerializedBrokeredIdentityContext userCtx) throws TbapiRegisterException {
        var user = getTbApiUser(formData);
        user.getAttributes().forEach(userCtx::setAttribute);
    }

    private User getTbApiUser(MultivaluedMap<String, String> formData) throws TbapiRegisterException {

        String phone = Util.getCleanUserPhone(formData.getFirst(FormConstants.FIELD_PHONE));

        User user = User.builder()
                .name(formData.getFirst(FormConstants.FIELD_FIRST_NAME))
                .email(formData.getFirst(FormConstants.FIELD_EMAIL))
                .phone(phone)
                .build();

        String orgName = formData.getFirst(FormConstants.FIELD_ORG_NAME);

        if (orgName == null) {
            orgName = formData.getFirst(FormConstants.FIELD_LAST_NAME);
        }
        user.getAttributes().put(ATTR_ORG_NAME, Collections.singletonList(orgName));


        Map<String, Object> attributes = tbapiService.registerUser(user, TbapiConnectConfig.getStaticConfig());

        userExtension.extendUser(user, attributes);

        return user;
    }
}
