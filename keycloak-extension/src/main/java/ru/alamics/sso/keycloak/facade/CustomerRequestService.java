package ru.alamics.sso.keycloak.facade;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.customer.CustomerDto;
import ru.alamics.sso.customer.CustomerService;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.tbapi.TbapiService;
import ru.alamics.sso.registration.tbapi.model.TbapiConnect;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.remote.tbapi.TbapiServiceRestImpl;

import javax.ejb.Singleton;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Singleton
public class CustomerRequestService {
    private static final String TBAPI_REQUEST_MAX_SIZE_PROPERTY = "tbapi.customer.request.max.size";
    private static final int TBAPI_REQUEST_MAX_SIZE = 10;
    private static final String LOAD_COEFF_PROPERTY = "tbapi.customer.request.load.coeff";
    private static final int LOAD_COEFF_DEFAULT = 100;
    private static final String TBAPI_CUSTOMER_DONT_REQUEST = "tbapi.customer.dont.request";

    private int tbapiRequestMaxSize;
    private int loadCoeff;
    private boolean dontRequest = false;

    private TbapiService tbapiService;
    private CustomerService customerService;
    private LinkedBlockingQueue<String> tomsIdQueue = new LinkedBlockingQueue<>();
    private ApplicationProperties properties;

    public CustomerRequestService() {
        tbapiService = new TbapiService(new TbapiServiceRestImpl());
        properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);
        customerService = (CustomerService) Lookup.lookup(CustomerService.class);
        tbapiRequestMaxSize = properties.getPropertyInt(TBAPI_REQUEST_MAX_SIZE_PROPERTY, TBAPI_REQUEST_MAX_SIZE, "CustomerRequestService: default value used: '%s' = '%s'");
        loadCoeff = properties.getPropertyInt(LOAD_COEFF_PROPERTY, LOAD_COEFF_DEFAULT, "CustomerRequestService: default value used: '%s' = '%s'");

        dontRequest = Boolean.parseBoolean(properties.getProperty(TBAPI_CUSTOMER_DONT_REQUEST));
    }

    public Map<String, String> updateCustomerNames() {
        List<String> currentTomsIds = extractListFromQueue(tbapiRequestMaxSize);
        if (currentTomsIds.isEmpty()) {
            return new HashMap<>();
        }
        try {
            Map<String, Object> customerMap = requestCustomerNames(currentTomsIds);
            Map<String, String> customers = new HashMap<>();
            for (String tomsId : currentTomsIds) {
                CustomerDto customer = CustomerDto.builder()
                        .tomsId(tomsId)
                        .name(customerMap.get(tomsId) == null ? " " : customerMap.get(tomsId).toString())
                        .build();
                customerService.save(customer);
                customers.put(customer.getTomsId(), customer.getName());
            }
            return customers;
        } catch (Exception e) {
            log.error("Fail getting customer names by tomsIds={}", currentTomsIds.toString(), e);
            return new HashMap<>();
        }
    }

    private Map<String, Object> requestCustomerNames(List<String> currentTomsIds) {

        if (dontRequest) {
            log.info("FAKE customer names request due to properties: customerIds={}", currentTomsIds);
            return new HashMap<>();
        }

        return tbapiService.customerNames(new TbapiConnectConfig(TbapiConnect.CUTOMER_NAMES), currentTomsIds);

    }

    public void addTomsIdsInQueue(List<String> updatingTomsId) {

        for (String tomsId : updatingTomsId) {
            tomsIdQueue.offer(tomsId);
        }
    }

    public int getLoadCoeff() {
        return tomsIdQueue.size() / tbapiRequestMaxSize / loadCoeff;
    }

    public int getTomsIdQueueSize() {
        return tomsIdQueue.size();
    }

    public int getTbapiRequestMaxSize() {
        return tbapiRequestMaxSize;
    }

    private List<String> extractListFromQueue(int countElements) {
        List<String> result = new LinkedList<>();
        if (tomsIdQueue.isEmpty()) {
            return Collections.emptyList();
        }

        int currentElement = 0;
        while (!tomsIdQueue.isEmpty() && currentElement <= countElements) {
            result.add(tomsIdQueue.poll());
            currentElement++;
        }
        return result;
    }

}
