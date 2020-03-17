package ru.alamics.sso.remote.tbapi;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.phone.SmsCodeGenerator;
import ru.alamics.sso.registration.tbapi.TbapiService;
import ru.alamics.sso.registration.tbapi.exception.TbapiRegisterException;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.registration.tbapi.model.TbapiRequest;
import ru.alamics.sso.registration.tbapi.model.TbapiResponse;
import ru.alamics.sso.registration.tbapi.port.TbapiRemoteService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_ORG_NAME;

@Slf4j
public class TbapiServiceRestImpl implements TbapiRemoteService {

    private static final Map<String, Object> mapExample = Collections.unmodifiableMap(new HashMap<>());

    private static final ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS);

    private static final ResteasyClient client = clientBuilder.build();

    @Override
    public TbapiResponse createCustomer(TbapiRequest request, TbapiConnectConfig connectConfig) throws TbapiRegisterException
    {
        // нужно кидать exception для показа страницы с ошибкой
        if (request != null && request.getLegalName() != null && request.getLegalName().equalsIgnoreCase("ПВФ Сейлор Мун")) {
            log.info("Во имя луны!");
            throw new TbapiRegisterException();
        }

        return createCustomerBattle(request, connectConfig);
    }

    private TbapiResponse createCustomerBattle(TbapiRequest request, TbapiConnectConfig connectConfig) throws TbapiRegisterException
    {

        URI uri = new ResteasyUriBuilder()
                .scheme(connectConfig.isSecure() ? "https" : "http")
                .host(connectConfig.getHost())
                .port(connectConfig.getPort())
                .path(connectConfig.getPath())
                .build();

        log.info("TBAPI request to {}", uri.toString());
        log.info("TBAPI config {}", connectConfig.toString());

        ResteasyWebTarget target = client.target(uri);

        Entity<TbapiRequest> entity = Entity.json(request);

        TbapiResponse responseData = null;
        try (Response response = target
                .register(ResteasyJackson2Provider.class) // TODO
                .register(StringTextStar.class)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .header("HOST", connectConfig.getHost())
                .header("Authorization", String.format("Trusted application=\"%s\", username=\"%s\"", connectConfig.getAppname(), connectConfig.getUsername()))
                .post(entity)) {

            log.info("response media type {}, status {}", response.getMediaType(), response.getStatus());

            if (response.getMediaType().toString().equalsIgnoreCase("text/html")) {
                log.error("response " + response.readEntity(String.class));
            } else {
                responseData = response.readEntity(TbapiResponse.class);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //responseMap = Map.of();
            throw new TbapiRegisterException(e);
        }

        return responseData;
    }

    @Override
    public Map<String, Object> getCustomerName(List<String> id, TbapiConnectConfig connectConfig) {
        log.info("customer names request : customerIds={}", id);
        Map<String, Object> responseMap = new HashMap<>();

        Response response = null;

        try {
            URI uri = new ResteasyUriBuilder()
                    .scheme(connectConfig.isSecure() ? "https" : "http")
                    .host(connectConfig.getHost())
                    .port(connectConfig.getPort())
                    .path(connectConfig.getPath())
                    .build();

            log.info("TBAPI request to {}", uri.toString());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", id);
            Entity<Map<String, Object>> entity = Entity.json(requestBody);

            ResteasyWebTarget target = client.target(uri);
            response = target.register(ResteasyJackson2Provider.class).request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("HOST", connectConfig.getHost())
                    .header("Authorization", String.format("Trusted application=\"%s\", username=\"%s\"", connectConfig.getAppname(), connectConfig.getUsername()))
                    .build("POST", entity)
                    .invoke();

            log.info("response media type {}, status {}", response.getMediaType(), response.getStatus());

            if (response.getMediaType().toString().equalsIgnoreCase("text/html")) {
                log.error("response " + response.readEntity(String.class));

            } else {

                responseMap = response.readEntity(new GenericType<>(mapExample.getClass()));
                if (responseMap.get("businessErrorCode") != null) {
                    throw new Exception("error tbapi code: " + responseMap.get("businessErrorCode").toString());
                }
            }

            log.info("customer names response : {}", responseMap);

        } catch (Exception e){
            log.error("tbapi error post request: ", e);
        } finally {

            if (response != null)
                response.close();
        }
        return responseMap;
    }

    public static void main(String[] args) {

        TbapiConnectConfig connectConfig = new TbapiConnectConfig();

        connectConfig.setHost("10.121.10.30");
        connectConfig.setPort(26800);
        connectConfig.setAppname("SSP");
        connectConfig.setUsername("anonymous");
        connectConfig.setPath("/api/v1/customerManagement/customerAccount");
        connectConfig.setSecure(false);

        User user = User.builder()
                .name("sdfs22dfsdf")
                .email("sdf22ds@gfsdgdfg.ru")
                .phone("71116460466")
                .build();

        String orgName = "dfgd22fgdfg";
        user.getAttributes().put(ATTR_ORG_NAME, Collections.singletonList(orgName));

        try {
            Map<String, Object> attributes = new TbapiService(new TbapiServiceRestImpl()).registerUser(user, connectConfig);
            System.out.println(attributes);
        } catch (TbapiRegisterException e) {
            e.printStackTrace();
        }
    }
}
