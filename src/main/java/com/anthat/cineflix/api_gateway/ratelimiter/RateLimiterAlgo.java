package com.anthat.cineflix.api_gateway.ratelimiter;

public interface RateLimiterAlgo {
    boolean isAllowed(String userIdentity);
}
