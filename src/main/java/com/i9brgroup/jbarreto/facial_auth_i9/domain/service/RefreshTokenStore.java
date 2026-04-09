package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.RefreshToken;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class RefreshTokenStore {

    private final Map<UUID, RefreshToken> activeTokens = new ConcurrentHashMap<>();
    private final Map<UUID, RefreshToken> blackList = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    private void init() {
        // Schedule blacklist cleanup: every 1 day
        scheduler.scheduleAtFixedRate(this::cleanBlackList, 1, 1, TimeUnit.DAYS);
        // Schedule active token cleanup: every 7 days
        scheduler.scheduleAtFixedRate(this::cleanExpiredTokens, 7, 7, TimeUnit.DAYS);
        log.info("RefreshTokenStore scheduled cleanup tasks initialized.");
    }

    public void store(RefreshToken token) {
        if (token == null || token.getId() == null) {
            throw new IllegalArgumentException("RefreshToken and its id must not be null");
        }
        activeTokens.put(token.getId(), token);
    }

    public RefreshToken get(UUID id) {
        return activeTokens.get(id);
    }

    public boolean isBlackListed(UUID id) {
        return blackList.containsKey(id);
    }

    public void revoke(UUID id) {
        RefreshToken token = activeTokens.remove(id);
        if (token != null) {
            blackList.put(id, token);
        }
    }

    private void cleanExpiredTokens() {
        Instant now = Instant.now();
        int removed = 0;
        Iterator<Map.Entry<UUID, RefreshToken>> iterator = activeTokens.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, RefreshToken> entry = iterator.next();
            RefreshToken token = entry.getValue();
            if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(now)) {
                iterator.remove();
                removed++;
            }
        }
        log.info("RefreshTokenStore cleaned {} expired tokens.", removed);
    }

    private void cleanBlackList() {
        int size = blackList.size();
        blackList.clear();
        log.info("RefreshTokenStore cleared blacklist ({} entries).", size);
    }
}
