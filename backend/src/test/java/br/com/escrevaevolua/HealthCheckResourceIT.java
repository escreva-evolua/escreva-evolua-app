package br.com.escrevaevolua;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthCheckResourceIT {

    @LocalServerPort
    int port;

    @Test
    void livenessIsUp() throws Exception {
        assertHealth("/actuator/health/liveness");
    }

    @Test
    void readinessIsUp() throws Exception {
        assertHealth("/actuator/health/readiness");
    }

    private void assertHealth(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(true, response.body().contains("\"status\":\"UP\""));
    }
}
