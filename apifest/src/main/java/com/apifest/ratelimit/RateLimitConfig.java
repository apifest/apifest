package com.apifest.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apifest.oauth20.ClientCredentials;
import com.apifest.oauth20.RateLimit;
import com.apifest.oauth20.persistence.DBManagerFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class RateLimitConfig {

    public static final long DEFAULT_CACHE_SIZE = 1024;

    private LoadingCache<String, RateLimit> cache = CacheBuilder.newBuilder().maximumSize(DEFAULT_CACHE_SIZE).build(new RateLimiLoader());

    private static volatile RateLimitConfig instance = null;
    private static Object lock =  new Object();

    private static Logger logger = LoggerFactory.getLogger(RateLimitConfig.class);

    private RateLimitConfig() {
    }

    public static RateLimitConfig getInstance() {
        RateLimitConfig local = instance;
        if(local == null) {
            synchronized (lock) {
                local = instance;
                if (local == null) {
                    local = new RateLimitConfig();
                    instance = local;
                }
            }
        }
        return local;
    }

    private static class RateLimiLoader extends CacheLoader<String, RateLimit> {

        @Override
        public RateLimit load(String clientId) throws Exception {
            logger.info("load RateLimit for clientId {}", clientId);
            ClientCredentials client = DBManagerFactory.getInstance().findClientCredentials(clientId);
            return (client == null || client.getRateLimit() == null) ? new RateLimit() : client.getRateLimit();
        }
    }

    public void reload(String clientId) {
        logger.info("invalidate cache for clientId {}", clientId);
        cache.invalidate(clientId);
    }

    public RateLimit getLimitByClientId(String clientId) {
        return cache.getUnchecked(clientId);
    }
}
