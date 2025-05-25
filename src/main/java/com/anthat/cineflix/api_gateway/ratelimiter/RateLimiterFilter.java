package com.anthat.cineflix.api_gateway.ratelimiter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//@Component
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {
    @Qualifier("tokenBucketRateLimiterAlgo")
    private final RateLimiterAlgo rateLimiterAlgo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        if (!path.equals("/api/v1/user/login") && !path.equals("/api/v1/user/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authenticationToken = SecurityContextHolder.getContext().getAuthentication();

        if (authenticationToken == null || !authenticationToken.isAuthenticated()) {
            return;
        }

        UserDetails userDetails = (UserDetails) authenticationToken.getPrincipal();
        if (userDetails == null) {
            return;
        }

        String userName = userDetails.getUsername();
        if (StringUtils.isBlank(userName)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (rateLimiterAlgo.isAllowed(userName)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded. Please try again later.");
        }

    }
}
