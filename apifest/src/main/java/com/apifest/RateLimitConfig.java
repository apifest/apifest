package com.apifest;

public class RateLimitConfig {

    public void load() {

    }

    public void reload() {
        // read the rate limit config from Redis(DB) and load it in Hazelcast
        // the rate limit will be per node, i.e. if a rate limit should be 60reqs/min for a client and there are
        // three nodes then the rate limit config will be 20 reqs/min.
    }

    public Long getLimitByClientId(String clientId) {
        return 5L;
    }
}
