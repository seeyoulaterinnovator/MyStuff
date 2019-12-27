package ru.alamics.sso.keycloak.facade;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.customer.CustomerDto;
import ru.alamics.sso.customer.CustomerService;
import ru.alamics.sso.keycloak.cache.CustomCache;
import ru.alamics.sso.keycloak.cache.UserPostCache;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.model.TbapiConstants;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.registration.tbapi.TbapiService;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.remote.tbapi.TbapiServiceRestImpl;

import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Stateless
public class UserPostFacade {
    private CustomCache<UserPostResponse> cache;
    private TbapiService tbapiService;
    private UserPostService userPostService;
    private CustomerService customerService;

    public UserPostFacade() {
        tbapiService = new TbapiService(new TbapiServiceRestImpl());
        try {
            this.userPostService = (UserPostService) new InitialContext().lookup("java:global/domru-sso/" + UserPostService.class.getSimpleName());
            this.cache = (CustomCache<UserPostResponse>) new InitialContext().lookup("java:global/domru-sso/" + UserPostCache.class.getSimpleName());
            this.customerService = (CustomerService) new InitialContext().lookup("java:global/domru-sso/" + CustomerService.class.getSimpleName());
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
    }

    public List<UserPostResponse> findByUserId(String userId) {
        Set<UserPostResponse> userPostCached = cache.getAll(userId);
        if (userPostCached == null) {
            try {
                List<UserPostResponse> userPosts = userPostService.getUserPost(userId);
                updateCustomers(userPosts, userId);
                return userPosts;
            } catch (NotFoundException e) {
                log.info(e.getMessage());
            }
        }
        return userPostCached.stream().collect(Collectors.toList());
    }

    private void updateCustomers(List<UserPostResponse> userPosts, String userId) {
        List<String> updatingTomsId = userPosts.stream()
                .filter(post -> post.getUpdateTime().isBefore(LocalDateTime.now().minusDays(1)) || post.getOrganization() == null)
                .map(UserPostResponse::getTomsId)
                .distinct()
                .collect(Collectors.toList());
        if (!updatingTomsId.isEmpty()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Map<String, Object> tomsMap = tbapiService.customerNames(connectConfig(), updatingTomsId);
                    userPosts.stream()
                            .filter(post -> updatingTomsId.contains(post.getTomsId()))
                            .forEach(post -> {
                                String newOrgName = post.getOrganization();
                                if (tomsMap.get(post.getTomsId()) != null) {
                                    newOrgName = tomsMap.get(post.getTomsId()).toString();
                                } else if (newOrgName == null) {
                                    newOrgName = " ";
                                }
                                post.setOrganization(newOrgName);
                                customerService.save(CustomerDto.builder()
                                        .tomsId(post.getTomsId())
                                        .name(newOrgName).build());
                            });
                    cache.put(userId, userPosts);
                }
            }).start();
        }
    }

    private TbapiConnectConfig connectConfig() {
        ApplicationProperties properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);
        TbapiConnectConfig connectConfig = new TbapiConnectConfig();
        if (properties != null) {
            connectConfig.setHost(properties.getProperty(TbapiConstants.HOST));
            connectConfig.setPort(Integer.parseInt(properties.getProperty(TbapiConstants.PORT)));
            connectConfig.setAppname(properties.getProperty(TbapiConstants.AUTH_APPNAME));
            connectConfig.setUsername(properties.getProperty(TbapiConstants.AUTH_USERNAME));
            connectConfig.setPath(properties.getProperty(TbapiConstants.CUSTOMER_FIND_PATH));
            connectConfig.setSecure(Boolean.parseBoolean(TbapiConstants.SECURE));
        }
        return connectConfig;
    }
}
