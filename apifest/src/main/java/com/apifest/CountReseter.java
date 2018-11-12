package com.apifest;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountReseter {

    private static Logger logger = LoggerFactory.getLogger(CountReseter.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public void resetCounters() {
        final Runnable reseter = new Runnable() {

            @Override
            public void run() {
                logger.info("run.............");
                AccessTokenCounter.getInstance().resetAllCounters();
            }
        };

       scheduler.scheduleAtFixedRate(reseter, 0L, ServerConfig.getRateLimitResetTimeinSec(), TimeUnit.SECONDS);
    }
}
