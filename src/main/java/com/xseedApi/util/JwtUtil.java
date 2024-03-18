package com.xseedApi.util;

import io.jsonwebtoken.Claims;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
	
	@Value("${jwt.secret}")
    private String secret;


    public void validateToken(final String token) {
    	 Claims claims = Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
 	    Date expirationDate = claims.getExpiration();
 	    
 	    if (expirationDate != null && expirationDate.before(new Date())) {
 	        throw new RuntimeException("Token has expired");
 	    }
        
    }



    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }
    
    public List<Integer> extractRoles(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
        return claims.get("roles", List.class);
    }
    
    public String extractUserId(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
        int userId = claims.get("userId", Integer.class);
        return String.valueOf(userId);
    }


}