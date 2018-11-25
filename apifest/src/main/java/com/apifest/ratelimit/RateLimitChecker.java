package com.apifest.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apifest.oauth20.RateLimit;

public class RateLimitChecker {

    private static Logger logger = LoggerFactory.getLogger(RateLimitConfig.class);

    private RateLimitChecker() {
    }

    public static boolean isRateOK(String clientId) {
        RateLimit rateLimit = RateLimitConfig.getInstance().getLimitByClientId(clientId);
        if (!rateLimit.isEmpty()) {
            Long limit = rateLimit.getRequests();
            Long currentCount = AccessTokenCounter.getInstance().getCount(clientId);
            if (currentCount.compareTo(limit) <= 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
