package ru.alamics.sso.keycloak.facade;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.customer.CustomerDto;
import ru.alamics.sso.customer.CustomerService;
import ru.alamics.sso.keycloak.cache.CustomCache;
import ru.alamics.sso.keycloak.cache.UserPostCache;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.dto.UserPostRequest;
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
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Slf4j
@Stateless
public class UserPostFacade {
    private static final String TBAPI_REQUEST_MAX_SIZE_PROPERTY = "tbapi.customer.request.max.size";
    private static final String TBAPI_REQUEST_INTERVAL_PROPERTY = "tbapi.customer.request.interval.milliseconds";
    private static final String CUSTOMER_CACHE_LIFESPAN_IN_DB_PROPERTY = "user.post.cache.db.lifespan.days";
    private int TBAPI_REQUEST_MAX_SIZE = 10;
    private int CUSTOMER_CACHE_LIFESPAN_IN_DB = 1;
    private long TBAPI_REQUEST_INTERVAL = 10000;
    private int tbapiRequestMaxSize;
    private int customerCacheLifespanInDb;
    private long tbapiRequestInterval;


    private CustomCache<UserPostResponse> cache;
    private TbapiService tbapiService;
    private UserPostService userPostService;
    private CustomerService customerService;
    private Thread threadTbapiRequest;
    private ConcurrentLinkedQueue<String> tomsIdQueue;
    private ApplicationProperties properties;

    public UserPostFacade() {
        tbapiService = new TbapiService(new TbapiServiceRestImpl());
        try {
            userPostService = (UserPostService) new InitialContext().lookup("java:global/domru-sso/" + UserPostService.class.getSimpleName());
            cache = (CustomCache<UserPostResponse>) new InitialContext().lookup("java:global/domru-sso/" + UserPostCache.class.getSimpleName());
            customerService = (CustomerService) new InitialContext().lookup("java:global/domru-sso/" + CustomerService.class.getSimpleName());
            properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);
            tbapiRequestMaxSize = Integer.parseInt(properties.getProperty(TBAPI_REQUEST_MAX_SIZE_PROPERTY));
            customerCacheLifespanInDb = Integer.parseInt(properties.getProperty(CUSTOMER_CACHE_LIFESPAN_IN_DB_PROPERTY));
            tbapiRequestInterval = Long.parseLong(properties.getProperty(TBAPI_REQUEST_INTERVAL_PROPERTY));
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        } catch (NumberFormatException e) {
            log.warn("Error parse properties file. All properties values set to default");
            tbapiRequestMaxSize = TBAPI_REQUEST_MAX_SIZE;
            customerCacheLifespanInDb = CUSTOMER_CACHE_LIFESPAN_IN_DB;
            tbapiRequestInterval = TBAPI_REQUEST_INTERVAL;
        }
    }

    public List<UserPostResponse> findByUserId(String userId) {
        Set<UserPostResponse> userPostCached = cache.get(userId);
        if (userPostCached == null || userPostCached.isEmpty()) {
            try {
                List<UserPostResponse> userPosts = userPostService.getUserPost(userId);
                updateCustomers(userPosts, userId);
                cache.put(userId, userPosts);
                return userPosts;
            } catch (NotFoundException e) {
                log.info(e.getMessage());
            }
        }
        return userPostCached.stream().collect(Collectors.toList());
    }

    public void addUserPostAndSystemRole(UserPostRequest userPostRequest) {
        cache.put(userPostRequest.getUserId(), List.of(userPostService.addUserPostAndSystemRole(userPostRequest)));
    }

    public void clearCacheByUserId(String userId) {
        cache.clearById(userId);
    }

    public void clearCache() {
        cache.clear();
    }

    private void updateCustomers(List<UserPostResponse> userPosts, String userId) {
        List<String> updatingTomsId = userPosts.stream()
                .filter(post -> post.getUpdateTime().isBefore(LocalDateTime.now().minusDays(customerCacheLifespanInDb)) || post.getOrganization() == null)
                .map(UserPostResponse::getTomsId)
                .distinct()
                .collect(Collectors.toList());
        if (!updatingTomsId.isEmpty()) {
            addTomsIdQueue(updatingTomsId);
            if (threadTbapiRequest != null && threadTbapiRequest.isAlive()) {
                return;
            }
            threadTbapiRequest = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("Started thread for request name organization to tbapi");
                    while (!tomsIdQueue.isEmpty()) {
                        List<String> currentUpdatingTomsIds = extractListFromQueue(tbapiRequestMaxSize);
                        try {
                            Map<String, Object> tomsMap = tbapiService.customerNames(connectConfig(), currentUpdatingTomsIds);
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
                            Thread.sleep(tbapiRequestInterval);
                        } catch (Exception e) {
                            log.error("Fail getting customer names by tomsIds={}", currentUpdatingTomsIds.toString());
                        }
                    }
                    log.info("Finished thread for request name organization to tbapi");
                }
            });
            threadTbapiRequest.start();
        }
    }

    private void addTomsIdQueue(List<String> updatingTomsId) {
        if (tomsIdQueue == null) {
            tomsIdQueue = new ConcurrentLinkedQueue<>();
        }
        updatingTomsId.forEach(tomsId -> tomsIdQueue.offer(tomsId));
    }

    private List<String> extractListFromQueue(int сountElements) {
        List<String> result = new LinkedList<>();
        if (tomsIdQueue == null || tomsIdQueue.isEmpty()) {
            return Collections.emptyList();
        }
        int currentElement = 0;
        while (!tomsIdQueue.isEmpty() && currentElement <= сountElements) {
            result.add(tomsIdQueue.poll());
            currentElement++;
        }
        return result;
    }

    private TbapiConnectConfig connectConfig() {
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
