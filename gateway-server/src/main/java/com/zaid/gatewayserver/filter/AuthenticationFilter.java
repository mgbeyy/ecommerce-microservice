package com.zaid.gatewayserver.filter;

import com.zaid.gatewayserver.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.*;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver exceptionResolver;

    private final List<String> openApiEndpoints = List.of(
            "/swagger-ui/**",
            "/api/v1/auth/**",
            "/eureka/**",
            "/v3/api-docs/**",
            "/**/v3/api-docs/**",
            "/docs/**"
    );

    public AuthenticationFilter(JwtUtil jwtUtil,
                                @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.jwtUtil = jwtUtil;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        boolean isApiOpen = openApiEndpoints.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));

        if (isApiOpen) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        String token = authHeader.substring(7);

        try {
            jwtUtil.validateToken(token);
            String userId = jwtUtil.extractUserId(token);
            String traceId = UUID.randomUUID().toString();

            MutableHttpServletRequest wrappedRequest = new MutableHttpServletRequest(request);
            wrappedRequest.addCustomHeader("X-User-Id", userId);
            wrappedRequest.addCustomHeader("Trace-Id", traceId);

            filterChain.doFilter(wrappedRequest, response);

        } catch (Exception e) {
            exceptionResolver.resolveException(request, response, null, e);
        }
    }

    private static class MutableHttpServletRequest extends HttpServletRequestWrapper {
        private final Map<String, String> customHeaders;

        public MutableHttpServletRequest(HttpServletRequest request) {
            super(request);
            this.customHeaders = new HashMap<>();
        }

        public void addCustomHeader(String name, String value) {
            this.customHeaders.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return headerValue;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> set = new HashSet<>(customHeaders.keySet());
            Enumeration<String> e = super.getHeaderNames();
            while (e.hasMoreElements()) {
                set.add(e.nextElement());
            }
            return Collections.enumeration(set);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return Collections.enumeration(Collections.singletonList(headerValue));
            }
            return super.getHeaders(name);
        }
    }
}