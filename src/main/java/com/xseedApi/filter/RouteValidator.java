package com.xseedApi.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
//foe what endpoints we need to validate for this purpose create RouteValidator class 
@Component
public class RouteValidator {
	//for below do not apply authentication filter  before token generation it doesn't make any sense 

    public static final List<String> openApiEndpoints = List.of(
            "/auth/**",
           "/reset-password/**",
            "/eureka"
    );

//    public Predicate<ServerHttpRequest> isSecured =
//            request -> openApiEndpoints
//                    .stream()
//                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

    public Predicate<ServerHttpRequest> isSecured =
            request -> {
                if (openApiEndpoints.stream().anyMatch(uri -> request.getURI().getPath().contains(uri))) {
                    return false; // Do not apply authentication filter for open API endpoints
                } else {
                    return hasRequiredRole(request);
                }
            };

    private boolean hasRequiredRole(ServerHttpRequest request) {
        // Extract roles from the request or use another mechanism
        Set<String> userRoles = extractRolesFromRequest(request);

        // Add role-based checks based on the request URI
        if (request.getURI().getPath().startsWith("/authentication/**")) {
            // Check if the user has the necessary role
            return userRoles.contains("ROLE_USER");
        }

        // Add more role-based checks for other secure endpoints
        // Example: Check for a different role for a different endpoint
//        if (request.getURI().getPath().startsWith("/anotherSecureEndpoint")) {
//            return userRoles.contains("ROLE_ADMIN");
//        }

        // Return false if no specific role-based check is required
        return false;
    }

    private Set<String> extractRolesFromRequest(ServerHttpRequest request) {
        // Implement logic to extract roles from the request
        // You might extract roles from headers, tokens, or another source
        // For example, if roles are stored in headers:
        List<String> roleHeaders = request.getHeaders().get("X-Roles");
        return new HashSet<>(roleHeaders != null ? roleHeaders : Collections.emptyList());
    }
}
