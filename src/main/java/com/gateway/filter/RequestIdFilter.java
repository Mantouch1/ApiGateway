package com.gateway.filter;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Gives every request a correlation id (header X-Request-Id): kept if the client sent a sane one, generated otherwise.
 * The id is forwarded to the microservices and returned to the client, so one request can be followed in all the logs.
 */
@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

	public static final String HEADER = "X-Request-Id";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String id = exchange.getRequest().getHeaders().getFirst(HEADER);
		if (id == null || !id.matches("[A-Za-z0-9._-]{1,100}")) {
			id = UUID.randomUUID().toString();
		}
		ServerHttpRequest request = exchange.getRequest().mutate().header(HEADER, id).build();
		exchange.getResponse().getHeaders().set(HEADER, id);
		return chain.filter(exchange.mutate().request(request).build());
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
