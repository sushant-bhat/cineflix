package com.anthat.cineflix.api_gateway.ratelimiter;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TokenBucket {
    private int capacity;

    private int refillRate;

    private int tokens;

    private long lastRefillTime;

    public TokenBucket() {
    }

    public TokenBucket(int capacity, int refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = capacity;
        lastRefillTime = System.currentTimeMillis();
    }
}
