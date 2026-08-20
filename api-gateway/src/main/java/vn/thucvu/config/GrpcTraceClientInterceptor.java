package vn.thucvu.config;

import io.grpc.*;
import io.micrometer.tracing.Tracer;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration(proxyBeanMethods = false)
@GrpcGlobalClientInterceptor
public class GrpcTraceClientInterceptor implements ClientInterceptor {

    public static final Metadata.Key<String> TRACE_ID_KEY = Metadata.Key.of("x-trace-id", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> SPAN_ID_KEY = Metadata.Key.of("x-span-id", Metadata.ASCII_STRING_MARSHALLER);

    private final Tracer tracer;

    public GrpcTraceClientInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                String traceId = null;
                String spanId = null;

                if (tracer != null && tracer.currentSpan() != null) {
                    traceId = Objects.requireNonNull(tracer.currentSpan()).context().traceId();
                    spanId = Objects.requireNonNull(tracer.currentSpan()).context().spanId();
                } else {
                    traceId = MDC.get("traceId");
                    spanId = MDC.get("spanId");
                }

                if (traceId != null) {
                    headers.put(TRACE_ID_KEY, traceId);
                }
                if (spanId != null) {
                    headers.put(SPAN_ID_KEY, spanId);
                }

                super.start(responseListener, headers);
            }
        };
    }
}
