package br.com.escrevaevolua.identity.adapter.http;

import java.io.IOException;
import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** In-process abuse control for unauthenticated invitation endpoints. Use a trusted edge limiter as well. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public final class PublicAuthRateLimitFilter extends OncePerRequestFilter {
    private static final long WINDOW_MILLIS = 60_000;
    private static final int MAX_BUCKETS = 10_000;
    private final int requestsPerMinute;
    private final Clock clock;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Autowired
    public PublicAuthRateLimitFilter(@Value("${identity.public-auth.rate-limit-per-minute:10}") int requestsPerMinute) {
        this(requestsPerMinute, Clock.systemUTC());
    }

    PublicAuthRateLimitFilter(int requestsPerMinute, Clock clock) {
        if (requestsPerMinute < 1) throw new IllegalArgumentException("Rate limit must be positive");
        this.requestsPerMinute = requestsPerMinute;
        this.clock = clock;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getServletPath();
        return !"/api/v1/auth/register".equals(path)
                && !"/api/v1/auth/invitations/validate".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long now = clock.millis();
        String key = request.getRemoteAddr() + ':' + request.getServletPath();
        Bucket bucket = buckets.compute(key, (ignored, current) -> {
            if (current == null || now - current.windowStart >= WINDOW_MILLIS) return new Bucket(now);
            return current;
        });
        int count = bucket.count.incrementAndGet();
        if (buckets.size() > MAX_BUCKETS) {
            buckets.entrySet().removeIf(entry -> now - entry.getValue().windowStart >= WINDOW_MILLIS);
            if (buckets.size() > MAX_BUCKETS) {
                reject(response, 60);
                return;
            }
        }
        if (count > requestsPerMinute) {
            reject(response, Math.max(1, (int) Math.ceil((bucket.windowStart + WINDOW_MILLIS - now) / 1000.0)));
            return;
        }
        chain.doFilter(request, response);
    }

    private static void reject(HttpServletResponse response, int retryAfterSeconds) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", Integer.toString(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"Muitas tentativas. Tente novamente mais tarde.\"}");
    }

    private static final class Bucket {
        private final long windowStart;
        private final AtomicInteger count = new AtomicInteger();
        private Bucket(long windowStart) { this.windowStart = windowStart; }
    }
}
