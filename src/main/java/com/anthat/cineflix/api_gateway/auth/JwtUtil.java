package com.anthat.cineflix.api_gateway.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.secret.key}")
    private String secretKey;

    @Value("${app.expiration.time}")
    private long expirationTime;

    public String extractJwtToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        return authorizationHeader.split(" ")[1];
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key())
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public boolean isValidJwtToken(String jwtToken, UserDetails userDetails) {
        final String userName = extractUserName(jwtToken);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(jwtToken);
    }

    private boolean isTokenExpired(String jwtToken) {
        return Jwts.parser()
                .verifyWith((SecretKey) key()).build()
                .parseSignedClaims(jwtToken).getPayload()
                .getExpiration()
                .before(new Date());
    }

    public String extractUserName(String jwtToken) {
        return Jwts.parser()
                .verifyWith((SecretKey) key()).build()
                .parseSignedClaims(jwtToken).getPayload()
                .getSubject();
    }
}
