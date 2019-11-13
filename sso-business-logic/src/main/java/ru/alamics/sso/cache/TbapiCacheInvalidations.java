package ru.alamics.sso.cache;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.cache.impl.TbapiCacheImpl;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
@Slf4j
public class TbapiCacheInvalidations {
    private TbapiCache cache = TbapiCacheImpl.getInstance();


    @Schedule(minute = "*/15", hour = "*")
    public void invalidations() {
        final String DEBUG_STR = "invalidations";
        log.info("start {}", DEBUG_STR);
        cache.invalidateCache();
        log.info("stop {}", DEBUG_STR);
    }
}
