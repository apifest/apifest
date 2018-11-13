package com.apifest.ratelimit;

public interface Counter {
    Long getCount(Object counterKey);

    Long increment(Object counterKey, Long value);

    void resetAllCounters();
}
