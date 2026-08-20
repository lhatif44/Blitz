package com.blitz.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    // throttles brute-force key guessing: at most this many failed attempts per source IP
    // within the window before further attempts are rejected outright with 429
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long WINDOW_MILLIS = 60_000;

    @Value("${app.admin.api-key:}")
    private String configuredApiKey;

    private final RateLimiter rateLimiter;

    public ApiKeyAuthFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        if ("GET".equals(method) || "OPTIONS".equals(method) || "HEAD".equals(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader(API_KEY_HEADER);

        if (StringUtils.hasText(configuredApiKey) && matches(configuredApiKey, providedKey)) {
            var auth = new UsernamePasswordAuthenticationToken(
                    "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
            return;
        }

        // only failed/missing-key attempts reach here (a successful match already returned
        // above), so this only throttles genuine guessing, never legitimate traffic
        if (!rateLimiter.allow(request.getRemoteAddr(), MAX_FAILED_ATTEMPTS, WINDOW_MILLIS)) {
            writeTooManyRequests(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    // Constant-time comparison — String.equals() short-circuits on the first mismatched
    // character, which leaks timing information an attacker can use to recover the key
    // byte-by-byte. MessageDigest.isEqual() is specified to run in time independent of
    // where (or whether) the inputs differ.
    private boolean matches(String configured, String provided) {
        if (provided == null) return false;
        byte[] configuredBytes = configured.getBytes(StandardCharsets.UTF_8);
        byte[] providedBytes = provided.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(configuredBytes, providedBytes);
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(WINDOW_MILLIS / 1000));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format(
                "{\"status\":429,\"message\":\"Too many failed authentication attempts. Try again later.\",\"timestamp\":\"%s\"}",
                Instant.now()));
    }
}
