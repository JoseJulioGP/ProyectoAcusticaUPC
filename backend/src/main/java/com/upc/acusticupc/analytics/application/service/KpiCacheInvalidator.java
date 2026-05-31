package com.upc.acusticupc.analytics.application.service;

import com.upc.acusticupc.shared.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class KpiCacheInvalidator {

    @CacheEvict(cacheNames = CacheConfig.KPIS_CACHE, allEntries = true)
    public void invalidateAll() {
        // Spring AOP ejecuta el evict al invocar este método
    }
}
