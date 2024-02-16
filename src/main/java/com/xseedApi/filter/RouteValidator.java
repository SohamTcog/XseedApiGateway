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

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

}
