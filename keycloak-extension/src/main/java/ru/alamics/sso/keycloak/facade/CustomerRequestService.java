package ru.alamics.sso.keycloak.facade;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.customer.CustomerDto;
import ru.alamics.sso.customer.CustomerService;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.model.TbapiConstants;
import ru.alamics.sso.registration.tbapi.TbapiService;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.remote.tbapi.TbapiServiceRestImpl;

import javax.ejb.Singleton;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Singleton
public class CustomerRequestService {
    private static final String TBAPI_REQUEST_MAX_SIZE_PROPERTY = "tbapi.customer.request.max.size";
    private int TBAPI_REQUEST_MAX_SIZE = 10;
    private int tbapiRequestMaxSize;

    private TbapiService tbapiService;
    private CustomerService customerService;
    private ConcurrentLinkedQueue<String> tomsIdQueue;
    private ApplicationProperties properties;

    public CustomerRequestService() {
        tbapiService = new TbapiService(new TbapiServiceRestImpl());
        try {
            properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);
            customerService = (CustomerService) Lookup.lookup(CustomerService.class);
            tbapiRequestMaxSize = Integer.parseInt(properties.getProperty(TBAPI_REQUEST_MAX_SIZE_PROPERTY));
        } catch (NumberFormatException e) {
            log.warn("Error parse properties file. All properties values set to default");
            tbapiRequestMaxSize = TBAPI_REQUEST_MAX_SIZE;
        }
    }

    public List<CustomerDto> updateCustomerNames() {
        List<String> currentUpdatingTomsIds = extractListFromQueue(tbapiRequestMaxSize);
        if (currentUpdatingTomsIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            Map<String, Object> customerMap = tbapiService.customerNames(connectConfig(), currentUpdatingTomsIds);
            List<CustomerDto> customers = new LinkedList<>();
            for (String tomsId : currentUpdatingTomsIds) {
                customers.add(customerService.save(CustomerDto.builder()
                        .tomsId(tomsId)
                        .name(customerMap.get(tomsId) == null ? " " : customerMap.get(tomsId).toString()).build()));
            }
            return customers;
        } catch (Exception e) {
            log.error("Fail getting customer names by tomsIds={}", currentUpdatingTomsIds.toString(), e);
            return Collections.emptyList();
        }
    }

    public void addTomsIdsInQueue(List<String> updatingTomsId) {
        if (tomsIdQueue == null) {
            tomsIdQueue = new ConcurrentLinkedQueue<>();
        }
        updatingTomsId.stream()
                .filter(tomsId -> !tomsIdQueue.contains(tomsId))
                .forEach(tomsId -> tomsIdQueue.offer(tomsId));
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
