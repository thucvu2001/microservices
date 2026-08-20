package vn.thucvu.config;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class TraceResponseFilter implements Filter {

    public static final String TRACE_HEADER = "X-Trace-Id";
    private final Tracer tracer;

    public TraceResponseFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (response instanceof HttpServletResponse httpServletResponse) {
            String traceId = null;
            if (tracer != null && tracer.currentSpan() != null) {
                traceId = Objects.requireNonNull(tracer.currentSpan()).context().traceId();
            }
            if (traceId == null) {
                traceId = MDC.get("traceId");
            }
            if (traceId != null) {
                httpServletResponse.setHeader(TRACE_HEADER, traceId);
            }
        }

        chain.doFilter(request, response);
    }
}
