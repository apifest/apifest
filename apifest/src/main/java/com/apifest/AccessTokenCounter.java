package com.apifest;

import java.util.Map.Entry;
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
        counters = new ConcurrentHashMap();
    }

    @Override
    public Long getCount(Object counterKey) {
        return counters.get(counterKey).get();
    }

    @Override
    public Long increment(Object clientId, Long value) {
        // we will store the data for a clientId
        AtomicLong count = counters.get(clientId);
        if (count == null) {
            count = new AtomicLong();
            counters.put(clientId, count);
        }
        // increment count per client per minute, get the minute of the timestamp and if there is a record for that minute increment the count,
        // delete the previous record
        System.out.println("increment count for clientId: " + clientId.toString());
        count.getAndAdd(value);
        return count.get();
    }

    @Override
    public void resetAllCounters() {
        // TODO: is that OK?
        for (Entry<Object, AtomicLong> counter :  counters.entrySet()) {
            logger.info("reset counter for clientId {}", counter.getKey());
            counters.put(counter.getKey(), new AtomicLong());
        }
        logger.info("reset all counters");
    }
}
