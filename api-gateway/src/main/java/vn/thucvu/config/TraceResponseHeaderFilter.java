package vn.thucvu.config;

import io.micrometer.tracing.Tracer;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class TraceResponseHeaderFilter implements GlobalFilter, Ordered {

    public static final String TRACE_HEADER = "X-Trace-Id";
    private final Tracer tracer;

    public TraceResponseHeaderFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getResponse().beforeCommit(() -> {
            String traceId = null;
            if (tracer != null && tracer.currentSpan() != null) {
                traceId = Objects.requireNonNull(tracer.currentSpan()).context().traceId();
            }
            if (traceId == null) {
                traceId = MDC.get("traceId");
            }
            if (traceId != null) {
                exchange.getResponse().getHeaders().set(TRACE_HEADER, traceId);
            }
            return Mono.empty();
        });

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
