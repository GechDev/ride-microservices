package com.ride.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Logs every request handled by the gateway together with the response status
 * and the total processing time.
 */
@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final String START_TIME_ATTRIBUTE = LoggingFilter.class.getName() + ".startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(START_TIME_ATTRIBUTE, startTime);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpMethod method = exchange.getRequest().getMethod();
            String path = exchange.getRequest().getPath().value();
            HttpStatusCode status = exchange.getResponse().getStatusCode();
            long duration = System.currentTimeMillis() - startTime;

            log.info("{} {} -> {} ({} ms)",
                    method != null ? method.name() : "UNKNOWN",
                    path,
                    status != null ? status.value() : "UNKNOWN",
                    duration);
        }));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
