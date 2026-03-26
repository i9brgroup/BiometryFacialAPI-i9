package com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // Define os nomes dos caches que sua aplicação vai usar
        // O nome "buckets" deve ser igual ao definido no application.yaml
        cacheManager.setCacheNames(Arrays.asList("buckets"));
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS) // Tempo de expiração dos buckets
                .maximumSize(1000); // Tamanho máximo do cache
    }
}
