package com.apifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apifest.oauth20.RateLimit;

public class RateLimitChecker {

    private static Logger logger = LoggerFactory.getLogger(RateLimitConfig.class);

    public static boolean isRateOK(String clientId) {
        RateLimit rateLimit = RateLimitConfig.getInstance().getLimitByClientId(clientId);
        logger.info("rateLimit for clientId {} is {}", clientId, rateLimit.getRequests());
        if (!rateLimit.isEmpty()) {
            Long limit = rateLimit.getRequests();
            Long currentCount = AccessTokenCounter.getInstance().getCount(clientId);
            logger.info("currentCount for clientId {} is {}", clientId, currentCount);
            if (currentCount.compareTo(limit) <= 0) {
                return true;
            }
            return false;
        } else {
            return true;
        }
    }
}
