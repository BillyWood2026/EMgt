package com.example.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * API Gateway Application
 *
 * This is an OPTIONAL infrastructure module.
 * If not installed, services will run with direct HTTP access.
 *
 * Features:
 * - Dynamic route discovery via Nacos
 * - Request routing and load balancing
 * - Rate limiting and flow control (Sentinel)
 * - Global authentication/authorization filter with basic JWT validation
 */
@SpringBootApplication
@EnableDiscoveryClient
public class APIGatewayApplication {

    private static final Logger log = LoggerFactory.getLogger(APIGatewayApplication.class);

    @Value("${gateway.jwt.secret:default-secret-key-change-in-production}")
    private String jwtSecret;

    public static void main(String[] args) {
        SpringApplication.run(APIGatewayApplication.class, args);
    }

    /**
     * Global authentication filter with basic JWT validation.
     * Non-blocking: requests without tokens are allowed through as anonymous.
     * Valid JWT tokens have user info extracted and set as exchange attributes.
     * Invalid tokens are logged but do not block requests (degraded mode).
     */
    @Bean
    @Order(1)
    public GlobalFilter authFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            String token = request.getHeaders().getFirst("Authorization");

            if (token == null || token.isBlank()) {
                log.debug("No Authorization header for request: {}", path);
                return chain.filter(exchange);
            }

            if (!token.startsWith("Bearer ")) {
                log.warn("Malformed Authorization header for request: {}", path);
                return chain.filter(exchange);
            }

            String jwt = token.substring(7).trim();

            try {
                Jws<Claims> claimsJws = Jwts.parser()
                        .setSigningKey(jwtSecret.getBytes())
                        .parseClaimsJws(jwt);

                Claims body = claimsJws.getBody();
                String username = body.getSubject();
                String role = body.get("role", String.class);

                exchange.getAttributes().put("userId", username);
                exchange.getAttributes().put("userRole", role);
                exchange.getAttributes().put("authenticated", true);

                log.info("JWT validated successfully: user={}, role={}, path={}", username, role, path);

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("JWT expired for request: {}, error: {}", path, e.getMessage());
                exchange.getAttributes().put("authenticated", false);
            } catch (io.jsonwebtoken.SignatureException e) {
                log.warn("JWT signature validation failed for request: {}, error: {}", path, e.getMessage());
                exchange.getAttributes().put("authenticated", false);
            } catch (Exception e) {
                log.warn("JWT validation error for request: {}, error: {}", path, e.getMessage());
                exchange.getAttributes().put("authenticated", false);
            }

            return chain.filter(exchange);
        };
    }
}