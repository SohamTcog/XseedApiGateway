package com.xseedApi.filter;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.xseedApi.util.JwtUtil;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
	
	  @Autowired
	    private RouteValidator validator;

	    @Autowired
	    private JwtUtil jwtUtil;

	    public AuthenticationFilter() {
	        super(Config.class);
	    }
	    
	  
	    @Override
	    public GatewayFilter apply(Config config) {
	        return ((exchange, chain) -> {
	            if (validator.isSecured.test(exchange.getRequest())) {
	                //header contains token or not
	                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
	                    throw new RuntimeException("missing authorization header");
	                }

	                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
	                if (authHeader != null && authHeader.startsWith("Bearer")) {
	                    authHeader = authHeader.substring(7);//remove empty space by default postman has it 
	                    System.out.print(authHeader);
	                    // Decode the token and extract roles
	                    List<Integer> roleIds = jwtUtil.extractRoles(authHeader);
	                    
	                    
//	                    System.out.print(roleIds);
	//
//	                    // Check if the user has the necessary role to access the endpoint
//	                    if (!roleIds.contains(5)) { // Adjust the role check as needed
//	                        throw new RuntimeException("Insufficient privileges");
//	                    }
	                    
	                    
	                    
	                    // Extract the path of the requested endpoint
	                    String path = exchange.getRequest().getPath().value();

	                    // Check roles based on the endpoint
	                    if (path.startsWith("/job/candidate") && !roleIds.contains(1)) {
	                        throw new RuntimeException("Insufficient privileges");
	                    } else if (path.startsWith("/job/recruiter") && !roleIds.contains(2)) {
	                        throw new RuntimeException("Insufficient privileges");
	                    } else if (path.startsWith("/job/admin") && !roleIds.contains(3)) {
	                        throw new RuntimeException("Insufficient privileges");
	                    }
	                }
	                try {
	                    // Validate the token
	                    jwtUtil.validateToken(authHeader);
	                } catch (Exception e) {
	                    System.out.println("invalid access...!");
	                    throw new RuntimeException("unauthorized access to application");
	                }
	            }
	            return chain.filter(exchange);
	        });
	    }

	    public static class Config {

	    }
}