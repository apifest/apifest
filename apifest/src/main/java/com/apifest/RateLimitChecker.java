package com.apifest;

public class RateLimitChecker {

    private static RateLimitConfig rateLimitConfig = new RateLimitConfig();

    public static boolean isRateOK(String clientId) {
        Long limit = rateLimitConfig.getLimitByClientId(clientId);
        Long currentCount = AccessTokenCounter.getInstance().getCount(clientId);
        System.out.println("currentCount for clientId " + clientId + " is: " + currentCount);
        if (currentCount.compareTo(limit) <= 0) {
            return true;
        }
        return false;
    }
}
