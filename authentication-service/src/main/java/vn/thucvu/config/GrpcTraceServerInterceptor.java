package vn.thucvu.config;

import io.grpc.*;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@GrpcGlobalServerInterceptor
public class GrpcTraceServerInterceptor implements ServerInterceptor {

    public static final Metadata.Key<String> TRACE_ID_KEY = Metadata.Key.of("x-trace-id", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> SPAN_ID_KEY = Metadata.Key.of("x-span-id", Metadata.ASCII_STRING_MARSHALLER);

    private final Tracer tracer;

    public GrpcTraceServerInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String traceId = headers.get(TRACE_ID_KEY);
        String spanId = headers.get(SPAN_ID_KEY);

        if (traceId != null) {
            MDC.put("traceId", traceId);
        }
        if (spanId != null) {
            MDC.put("spanId", spanId);
        }

        Span span = null;
        Tracer.SpanInScope spanInScope = null;
        if (traceId != null && tracer != null) {
            try {
                TraceContext parentContext = tracer.traceContextBuilder()
                        .traceId(traceId)
                        .spanId(spanId != null ? spanId : traceId)
                        .sampled(true)
                        .build();
                span = tracer.spanBuilder()
                        .setParent(parentContext)
                        .name(call.getMethodDescriptor().getFullMethodName())
                        .start();
                spanInScope = tracer.withSpan(span);
            } catch (Exception ignored) {
            }
        }

        final Span activeSpan = span;
        final Tracer.SpanInScope activeScope = spanInScope;

        ServerCall.Listener<ReqT> listener = next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                try {
                    super.close(status, trailers);
                } finally {
                    if (activeSpan != null) {
                        activeSpan.end();
                    }
                    if (activeScope != null) {
                        activeScope.close();
                    }
                    MDC.remove("traceId");
                    MDC.remove("spanId");
                }
            }
        }, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            @Override
            public void onMessage(ReqT message) {
                if (traceId != null) {
                    MDC.put("traceId", traceId);
                }
                super.onMessage(message);
            }

            @Override
            public void onHalfClose() {
                if (traceId != null) {
                    MDC.put("traceId", traceId);
                }
                super.onHalfClose();
            }
        };
    }
}
