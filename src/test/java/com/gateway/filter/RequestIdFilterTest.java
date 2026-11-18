package com.gateway.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

class RequestIdFilterTest {

	private final RequestIdFilter filter = new RequestIdFilter();

	private ServerWebExchange run(MockServerHttpRequest request, MockServerWebExchange exchange) {
		AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
		GatewayFilterChain chain = ex -> {
			forwarded.set(ex);
			return Mono.empty();
		};
		filter.filter(exchange, chain).block();
		return forwarded.get();
	}

	@Test
	void generatesAnIdWhenMissing() {
		MockServerHttpRequest request = MockServerHttpRequest.get("/api/tenders/1").build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);
		ServerWebExchange forwarded = run(request, exchange);
		String id = forwarded.getRequest().getHeaders().getFirst(RequestIdFilter.HEADER);
		assertNotNull(id);
		assertEquals(id, exchange.getResponse().getHeaders().getFirst(RequestIdFilter.HEADER));
	}

	@Test
	void keepsAValidIncomingId() {
		MockServerHttpRequest request = MockServerHttpRequest.get("/api/x").header(RequestIdFilter.HEADER, "abc-123").build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);
		ServerWebExchange forwarded = run(request, exchange);
		assertEquals("abc-123", forwarded.getRequest().getHeaders().getFirst(RequestIdFilter.HEADER));
	}

	@Test
	void replacesAnInvalidIncomingId() {
		MockServerHttpRequest request = MockServerHttpRequest.get("/api/x").header(RequestIdFilter.HEADER, "bad id with spaces").build();
		MockServerWebExchange exchange = MockServerWebExchange.from(request);
		ServerWebExchange forwarded = run(request, exchange);
		assertNotEquals("bad id with spaces", forwarded.getRequest().getHeaders().getFirst(RequestIdFilter.HEADER));
	}
}
