package com.xseedApi.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
//for this add dependency ---> jwt dependency 
//Here use same methods as you created in JWTService in identity service 
//same key should be used to validate token 
@Component
public class JwtUtil {


    public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";


    public void validateToken(final String token) {
    	 Claims claims = Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
 	    Date expirationDate = claims.getExpiration();
 	    
 	    if (expirationDate != null && expirationDate.before(new Date())) {
 	        throw new RuntimeException("Token has expired");
 	    }
        
    }



    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }
    
    public List<Integer> extractRoles(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
        return claims.get("roles", List.class);
    }
}
