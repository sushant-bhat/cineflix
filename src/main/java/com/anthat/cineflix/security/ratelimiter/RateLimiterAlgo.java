package com.anthat.cineflix.security.ratelimiter;

public interface RateLimiterAlgo {
    boolean isAllowed(String userIdentity);
}
