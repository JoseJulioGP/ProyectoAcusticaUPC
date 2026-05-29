package com.upc.acusticupc.shared.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Caché en memoria (sin Redis) para aliviar la latencia de Supabase us-east-1
 * desde Colombia. TTL emulado con evicción programada cada 30s: suficiente
 * para el volumen del anteproyecto.
 */
@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    public static final String KPIS_CACHE = "analyticsKpis";

    private final ConcurrentMapCacheManager manager =
            new ConcurrentMapCacheManager(KPIS_CACHE);

    @Bean
    public CacheManager cacheManager() {
        return manager;
    }

    @Scheduled(fixedRate = 30_000)
    public void evictKpis() {
        var cache = manager.getCache(KPIS_CACHE);
        if (cache != null) cache.clear();
    }
}