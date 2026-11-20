package br.com.escrevaevolua.identity.adapter.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class PublicAuthRateLimitFilterTest {
    @Test
    void limitsPublicRegistrationByRemoteAddressAndEndpoint() throws Exception {
        var filter = new PublicAuthRateLimitFilter(2,
                Clock.fixed(Instant.parse("2026-10-31T00:00:00Z"), ZoneOffset.UTC));
        var chainCalls = new AtomicInteger();

        var first = request("/api/v1/auth/register", "192.0.2.1");
        filter.doFilter(first, new MockHttpServletResponse(), new MockFilterChain() {
            @Override public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                chainCalls.incrementAndGet();
            }
        });
        var secondResponse = new MockHttpServletResponse();
        filter.doFilter(request("/api/v1/auth/register", "192.0.2.1"), secondResponse, new MockFilterChain() {
            @Override public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                chainCalls.incrementAndGet();
            }
        });
        var limitedResponse = new MockHttpServletResponse();
        filter.doFilter(request("/api/v1/auth/register", "192.0.2.1"), limitedResponse, new MockFilterChain() {
            @Override public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                chainCalls.incrementAndGet();
            }
        });

        assertEquals(2, chainCalls.get());
        assertEquals(429, limitedResponse.getStatus());
        assertNotNull(limitedResponse.getHeader("Retry-After"));
        assertTrue(limitedResponse.getContentAsString().contains("Muitas tentativas"));
        assertEquals(200, secondResponse.getStatus());
    }

    @Test
    void doesNotTrustForwardedAddressHeaders() {
        var filter = new PublicAuthRateLimitFilter(1,
                Clock.fixed(Instant.parse("2026-10-31T00:00:00Z"), ZoneOffset.UTC));
        var first = request("/api/v1/auth/register", "192.0.2.1");
        first.addHeader("X-Forwarded-For", "198.51.100.10");
        var second = request("/api/v1/auth/register", "192.0.2.1");
        second.addHeader("X-Forwarded-For", "203.0.113.20");
        var response = new MockHttpServletResponse();

        try {
            filter.doFilter(first, new MockHttpServletResponse(), new MockFilterChain());
            filter.doFilter(second, response, new MockFilterChain());
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
        assertEquals(429, response.getStatus());
    }

    private static MockHttpServletRequest request(String path, String remoteAddress) {
        var request = new MockHttpServletRequest("POST", path);
        request.setServletPath(path);
        request.setRemoteAddr(remoteAddress);
        return request;
    }
}
