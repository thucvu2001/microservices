package vn.thucvu.config;

import com.google.gson.Gson;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import vn.thucvu.grpc.VerifyTokenGrpcResponse;
import vn.thucvu.model.PermissionHash;
import vn.thucvu.repository.PermissionRepository;
import vn.thucvu.request.CheckPermissionRequest;
import vn.thucvu.response.CheckPermissionResponse;
import vn.thucvu.response.ErrorResponse;
import vn.thucvu.response.PermissionResponse;
import vn.thucvu.response.VerifyTokenResponse;
import vn.thucvu.service.VerifyTokenService;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
@Slf4j(topic = "CUSTOMIZE-FILTER")
public class CustomizeFilter extends AbstractGatewayFilterFactory<CustomizeFilter.Config> {

    private final RestTemplate restTemplate;
    private final PermissionRepository permissionRepository;
    private final VerifyTokenService verifyTokenService;
    private final Tracer tracer;

    @Value("${service.authUrl}")
    private String authUrl;
    @Value("${service.authorUrl}")
    private String authorUrl;

    public CustomizeFilter(RestTemplate restTemplate, PermissionRepository permissionRepository, VerifyTokenService verifyTokenService, io.micrometer.tracing.Tracer tracer) {
        super(Config.class);
        this.restTemplate = restTemplate;
        this.permissionRepository = permissionRepository;
        this.verifyTokenService = verifyTokenService;
        this.tracer = tracer;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String url = request.getPath().toString();
            log.info("-------------[ {} ]", url);

            if (isWhiteListURL(url) || url.contains("/v3/api-docs/")) {
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                }));
            }

            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders requestHeaders = request.getHeaders();

            if (requestHeaders.containsKey(AUTHORIZATION) && Objects.requireNonNull(requestHeaders.getFirst(AUTHORIZATION)).startsWith("Bearer ")) {
                // Get access token from header
                final String token = request.getHeaders().getOrEmpty(AUTHORIZATION).get(0).substring(7);

                // verify access token
//                VerifyTokenResponse verifyTokenResponse = verifyAccessToken(token);
                VerifyTokenGrpcResponse grpcResponse = verifyTokenService.verifyAccessToken(token);
                if (!grpcResponse.getIsValid()) {
                    return printErrorMessage(exchange.getResponse(), HttpStatus.valueOf(grpcResponse.getStatus()), url, grpcResponse.getMessage());
                }

                // authorization
                // Option 1: check role
                 getRoleByUsername(grpcResponse.getUsername());

                // Option 2: check permission
                 getPermissionByUsername(grpcResponse.getUsername());

                // Option 3: check permission
                CheckPermissionResponse checkPermissionResponse = checkPermissionByUsernameAndRequestPath(grpcResponse.getUsername(), request.getMethod().name(), url);
                if (200 != checkPermissionResponse.getStatus()) {
                    return printErrorMessage(exchange.getResponse(), FORBIDDEN, url, checkPermissionResponse.getMessage());
                }

                boolean isGranted = checkRoleInCache(grpcResponse.getUsername());
                if (!isGranted) {
                    return printErrorMessage(exchange.getResponse(), FORBIDDEN, url, "Access denied");
                }

                log.info("Request valid");
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                }));
            } else {
                log.info("Request not valid, URL={}", url);
                return printErrorMessage(exchange.getResponse(), UNAUTHORIZED, url, "Request invalid, Please try again!");
            }
        };
    }

    /**
     * Check permission in Redis
     *
     * @param username
     * @return
     */
    private boolean checkRoleInCache(String username) {
        Optional<PermissionHash> data = permissionRepository.findById(username);
        if (data.isPresent()) {
            // to do something
            return true;
        }
        return false;
    }

    /**
     * Verify Access Token
     *
     * @param token
     * @return
     */
    VerifyTokenResponse verifyAccessToken(String token) {
        log.info("Verify access token");

        try {
            return restTemplate.postForObject(
                    authUrl + "/verify-token",
                    token,
                    VerifyTokenResponse.class
            );
        } catch (RestClientException e) {
            return VerifyTokenResponse.builder()
                    .status(UNAUTHORIZED.value())
                    .message(e.getMessage())
                    .isValid(false)
                    .build();
        }
    }

    /**
     * Check role by username
     *
     * @param username
     * @return
     */
    List<?> getRoleByUsername(String username) {
        log.info("checkRoleByUsername called");

        try {
            List<Long> roles = restTemplate.getForObject(
                    authorUrl + "/roles?username=" + username,
                    List.class
            );
            if (roles == null || roles.isEmpty()) {
                return List.of();
            }
            return roles;
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /**
     * Check permission by username
     *
     * @param username
     * @return
     */
    List<PermissionResponse> getPermissionByUsername(String username) {
        log.info("checkPermissionByUsername called");

        try {
            List<PermissionResponse> permissions = restTemplate.getForObject(
                    authorUrl + "/permissions?username=" + username,
                    List.class
            );
            if (permissions == null || permissions.isEmpty()) {
                return List.of();
            }

            return permissions;
        } catch (RestClientException e) {
            log.error(e.getMessage());
            return List.of();
        }
    }

    /**
     * Check permission by request Method + URL
     *
     * @param username
     * @param method
     * @param path
     * @return
     */
    private CheckPermissionResponse checkPermissionByUsernameAndRequestPath(String username, String method, String path) {
        log.info("checkPermissionByUsernameAndRequestPath");

        CheckPermissionRequest request = new CheckPermissionRequest();
        request.setUsername(username);
        request.setPath(method + " " + path);

        try {
            return restTemplate.postForObject(
                    authorUrl + "/check-permissions",
                    request,
                    CheckPermissionResponse.class
            );
        } catch (RestClientException e) {
            log.error("Can not connect to author-service: ", e);
            return CheckPermissionResponse.builder()
                    .status(UNAUTHORIZED.value())
                    .message(e.getMessage())
                    .path(request.getPath())
                    .build();
        }
    }

    /**
     * White list for access app without token
     *
     * @return
     */
    private boolean isWhiteListURL(String url) {
        return url.contains("/access-token")
                || url.contains("/refresh-token")
                || url.contains("/verify-token");
    }

    /**
     * @param response
     * @param url
     * @param message
     * @return
     */
    private Mono<Void> printErrorMessage(ServerHttpResponse response, HttpStatus status, String url, String message) {
        log.info("Request valid, URL={}", url);

        String traceId = null;
        if (tracer != null && tracer.currentSpan() != null) {
            traceId = Objects.requireNonNull(tracer.currentSpan()).context().traceId();
        }
        if (traceId == null) {
            traceId = org.slf4j.MDC.get("traceId");
        }
        if (traceId == null && tracer != null) {
            io.micrometer.tracing.Span span = tracer.nextSpan().start();
            traceId = span.context().traceId();
            span.end();
        }

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setPath(url);
        errorResponse.setStatus(status.value());
        errorResponse.setError(status.getReasonPhrase());
        errorResponse.setMessage(message);
        errorResponse.setTraceId(traceId);

        if (traceId != null) {
            response.getHeaders().set("X-Trace-Id", traceId);
        }

        byte[] bytes = new Gson().toJson(errorResponse).getBytes(StandardCharsets.UTF_8);

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.setStatusCode(HttpStatus.OK);

        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {

    }
}
