package com.apifest.ratelimit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessTokenCounter implements Counter {

    private ConcurrentMap<Object, AtomicLong> counters;

    private static volatile AccessTokenCounter instance = null;
    private static Object lock =  new Object();

    private static Logger logger = LoggerFactory.getLogger(AccessTokenCounter.class);

    public static AccessTokenCounter getInstance() {
        AccessTokenCounter local = instance;
        if(local == null) {
            synchronized (lock) {
                local = instance;
                if (local == null) {
                    local = new AccessTokenCounter();
                    instance = local;
                }
            }
        }
        return local;
    }

    private AccessTokenCounter() {
        counters = new ConcurrentHashMap<Object, AtomicLong>();
    }

    @Override
    public Long getCount(Object counterKey) {
        return counters.get(counterKey).get();
    }

    @Override
    public Long increment(Object clientId, Long value) {
        AtomicLong count = counters.computeIfAbsent(clientId, k -> new AtomicLong());
        logger.info("increment count for clientId: {}", clientId.toString());
        count.getAndAdd(value);
        return count.get();
    }

    @Override
    public void resetAllCounters() {
        counters = new ConcurrentHashMap<>();
        logger.info("reset all counters");
    }

}
