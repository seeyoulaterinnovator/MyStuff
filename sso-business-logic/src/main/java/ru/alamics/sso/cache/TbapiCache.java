package ru.alamics.sso.cache;

import java.util.List;
import java.util.Map;

public interface TbapiCache {

    void putToCache(String customerId, Object o);
    Object getCustomerNameFromCache(String customerId);
    Map<String, Object> getCustomerNamesFromCache(List<String> customer);

    void invalidateCache();
}
