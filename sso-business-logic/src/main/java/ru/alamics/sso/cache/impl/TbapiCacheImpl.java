package ru.alamics.sso.cache.impl;

import ru.alamics.sso.cache.TbapiCache;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class TbapiCacheImpl implements TbapiCache {
    private static final TbapiCache INSTANCE = new TbapiCacheImpl();
    private Map<String, LocalDateTime> invalidationsTime = new HashMap<>();
    private Map<String, Object> cache = new ConcurrentHashMap<>();
    private static final String CUSTOMER_NAME_PREFIX = ".customerName";

    private TbapiCacheImpl () {
    }

    @Override
    public void putToCache (String customerId, Object o) {
        if (o == null){
            return;
        }
        final String id = getCustomerCacheName(customerId);
        var formCache = getCustomerNameFromCache(id);
        if(formCache == null) {
            this.cache.put(id, o);
            this.invalidationsTime.put(id, LocalDateTime.now().plusMinutes(45));
        }
    }

    @Override
    public void deleteFromCache (String customerId) {
        this.cache.remove(getCustomerCacheName(customerId));
    }

    @Override
    public Object getCustomerNameFromCache (String customerId) {
        return this.cache.get(customerId);
    }

    @Override
    public Map<String, Object> getCustomerNamesFromCache (List<String> customers) {
        Map<String, Object> ret = new HashMap<>();
        customers.forEach(customer -> ret.put(customer, getCustomerNameFromCache(getCustomerCacheName(customer))));
        return ret;
    }

    @Override
    public void invalidateCache () {
        LocalDateTime now = LocalDateTime.now();
        var invalidate = this.invalidationsTime.entrySet().stream().filter(map -> map.getValue().isBefore(now)).map(Map.Entry::getKey).collect(Collectors.toList());
        invalidate.forEach(invalidCache -> {
            deleteFromCache(invalidCache);
            this.invalidationsTime.remove(invalidCache);
        });
    }

    public static TbapiCache getInstance () {
        return INSTANCE;
    }

    private String getCustomerCacheName (final String customerId) {
        return customerId + CUSTOMER_NAME_PREFIX;
    }
}
