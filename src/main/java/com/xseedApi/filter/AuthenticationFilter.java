package com.xseedApi.filter;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import com.xseedApi.constants.PathConstants;
import com.xseedApi.constants.RolesConstants;
import com.xseedApi.exception.XseedException;
import com.xseedApi.util.JwtUtil;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
	private static final Logger logger = Logger.getLogger(AuthenticationFilter.class.getName());
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
			ServerHttpRequest request = null;
			if (validator.isSecured.test(exchange.getRequest())) {
				// header contains token or not
				if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
					throw new XseedException("missing authorization header");
				}

				String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
				if (authHeader != null && authHeader.startsWith("Bearer ")) {
					authHeader = authHeader.substring(7);

					// Decode the token and extract roles
					List<Integer> roleIds = jwtUtil.extractRoles(authHeader);

					// Extract the path of the requested endpoint
					String path = exchange.getRequest().getPath().value();

					// Check if the requested path matches any of the defined paths for different
					// roles
					if (path.startsWith(PathConstants.CANDIDATE_PATH)
							&& !roleIds.contains(RolesConstants.ROLE_CANDIDATE)) {
						throw new RuntimeException("Insufficient privileges");
					} else if ((path.startsWith(PathConstants.RECRUITER_PATH)
							|| path.startsWith(PathConstants.RECRUITER_LISTING_PATH))
							&& !roleIds.contains(RolesConstants.ROLE_RECRUITER)) {
						throw new RuntimeException("Insufficient privileges");
					} else if (path.startsWith(PathConstants.ADMIN_PATH)
							|| path.startsWith(PathConstants.TEMPLATE_PATH)
							&& !roleIds.contains(RolesConstants.ROLE_ADMIN)) {

						throw new XseedException("Insufficient privileges");
					}

				}
				try {

					jwtUtil.validateToken(authHeader);
					 logger.info("\n\n\n Headers before modification: " + exchange.getRequest().getHeaders());

					String userId = jwtUtil.extractUserId(authHeader);

					if (request == null) {
						request = exchange.getRequest();
					}

					request = request.mutate().header("loggedInUser", userId).build();
					 logger.info("\n\n\n Headers after modification: " + request.getHeaders());
				} catch (Exception e) {
					 logger.info("invalid access...!");
					throw new RuntimeException("un authorized access to application");
				}
			}
			return chain.filter(exchange.mutate().request(request).build());
		});
	}

	public static class Config {

	}
}