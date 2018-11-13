package com.apifest.ratelimit;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.apifest.ServerConfig;
import com.apifest.ratelimit.AccessTokenCounter;
import com.apifest.ratelimit.CountReseter;

public class AccessTokenCounterTest {

    @Test
    public void when_resetCounters_reset_the_count() throws Exception {
        // GIVEN
        AccessTokenCounter counter = AccessTokenCounter.getInstance();
        ServerConfig.rateLimitResetTimeinSec = 5;
        String clientId = "34234adc342d01";

        // WHEN
        CountReseter reseter = new CountReseter();
        reseter.resetCounters();
        Long result = counter.increment(clientId, 1L);
        assertEquals(result, Long.valueOf(1L));
        result = counter.increment(clientId, 1L);
        assertEquals(result, Long.valueOf(2L));
        result = counter.increment(clientId, 1L);
        assertEquals(result, Long.valueOf(3L));
        Thread.sleep(ServerConfig.rateLimitResetTimeinSec * 1000 + 2);

        // THEN
        result = counter.increment(clientId, 1L);
        assertEquals(result, Long.valueOf(1));
    }

    @Test
    public void when_increment_increase_the_count() throws Exception {
        // GIVEN
        AccessTokenCounter counter = AccessTokenCounter.getInstance();
        String clientId = "34234adc342d01aaa";

        // WHEN
        Long result = counter.increment(clientId, 1L);
        assertEquals(result, Long.valueOf(1L));
        result = counter.increment(clientId, 1L);
        assertEquals(result, Long.valueOf(2L));

        // THEN
        assertEquals(counter.getCount(clientId), Long.valueOf(2L));
    }

}
