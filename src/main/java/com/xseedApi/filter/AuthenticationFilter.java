package com.xseedApi.filter;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                    
                    // Decode the token and extract roles
                   // List<?> roleIds = jwtUtil.extractRoles(authHeader);
                   // System.out.println(roleIds);
                    List<Map<String, Object>> roleIdList = jwtUtil.extractRoles(authHeader);

                    List<Integer> ids = roleIdList.stream()
                            .map(roleMap -> (int) roleMap.get("id"))
                            .collect(Collectors.toList());

                    List<String> roleNames = roleIdList.stream()
                            .map(roleMap -> (String) roleMap.get("role"))
                            .collect(Collectors.toList());
                    
                    System.out.println(ids);
                    System.out.println(roleNames);

                    String path = exchange.getRequest().getPath().value();
                    
                    
                    /*Retrieve the endpoint mappings from RoleEndpointConfig bean
                    Map<Integer, String> roleEndpointMap = roleEndpointConfig.roleEndpointMap();

                    // Check roles based on the endpoint using the configured mappings
                    for (Map.Entry<Integer, String> entry : roleEndpointMap.entrySet()) {
                        if (path.startsWith(entry.getValue()) && !roleIds.contains(entry.getKey())) {
                            throw new RuntimeException("Insufficient privileges");
                        }
                    }*/

                    //Check roles based on the endpoint
                    
                    
                    /*
                     * role id - 5 ---> candidate 
                     * role id 6-----> recruiter 
                     * role id 7 -----> admin 
                     * 8----> super admin 
                     * 9-----> delievery manager 
                     * please start paths accordingly in separate controller 
                     */
                    if (path.startsWith("/job/candidate") && !roleNames.contains("ROLE_ADMIN")) {
                        throw new RuntimeException("Insufficient privileges");
                    } else if (path.startsWith("/job/recruiter") && !roleNames.contains("ROLE_ADMIN")) {
                        throw new RuntimeException("Insufficient privileges");
                    } else if (path.startsWith("/job/admin") && !roleNames.contains("ROLE_USER")) {
                        throw new RuntimeException("Insufficient privileges");
                    }
                    
                  
                }
                try {
//                    //REST call to AUTH service
//                    template.getForObject("http://IDENTITY-SERVICE//validate?token" + authHeader, String.class);
                    jwtUtil.validateToken(authHeader);

                } catch (Exception e) {
                    System.out.println("invalid access...!");
                    throw new RuntimeException("un authorized access to application");

                }
            }
            return chain.filter(exchange);
        });
    }

	    public static class Config {

	    }
}