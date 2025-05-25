package com.anthat.cineflix.api_gateway.ratelimiter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TokenBucketRateLimiterAlgo implements RateLimiterAlgo {
    // TODO: Implement a central bucket system (redis) which has a bucket for each user created when onboarded
    private static final Map<String, TokenBucket> bucketSystem = new HashMap<>();

    @Value("${app.ratelimiter.capacity}")
    private int capacity;

    @Value("${app.ratelimiter.refillRate}")
    private int refillRate;

    @Override
    public synchronized boolean isAllowed(String userIdentity) {
        if (!bucketSystem.containsKey(userIdentity)) {
            bucketSystem.put(userIdentity, new TokenBucket(capacity, refillRate));
        }

        TokenBucket bucket = bucketSystem.get(userIdentity);
        refillTokens(bucket);

        if (bucket.getTokens() > 0) {
            bucket.setTokens(bucket.getTokens() - 1);
            return true;
        }
        return false;
    }

    private void refillTokens(TokenBucket bucket) {
        long now = System.currentTimeMillis();
        long elapsedTime = now - bucket.getLastRefillTime();

        if (elapsedTime > 0) {
            int newTokens = (int) ((elapsedTime / 1000) * bucket.getRefillRate());
            bucket.setTokens(Math.min(bucket.getCapacity(), bucket.getTokens() + newTokens));
            bucket.setLastRefillTime(now);
        }
    }
}
