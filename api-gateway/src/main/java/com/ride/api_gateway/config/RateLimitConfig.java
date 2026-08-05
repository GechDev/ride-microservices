package com.ride.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import reactor.core.publisher.Mono;

import java.security.Principal;

/**
 * Resolves the rate-limit key per request. Authenticated requests are bucketed
 * by the JWT subject; unauthenticated requests fall back to the client IP.
 */
@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(Principal::getName)
                .switchIfEmpty(Mono.just(clientIp(exchange)));
    }

    private String clientIp(org.springframework.web.server.ServerWebExchange exchange) {
        java.net.InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "anonymous";
    }
}
