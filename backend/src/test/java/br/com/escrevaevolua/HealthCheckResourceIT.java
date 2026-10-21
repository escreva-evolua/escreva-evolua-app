package br.com.escrevaevolua;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusIntegrationTest
class HealthCheckResourceIT {

    @Test
    void livenessIsUp() {
        assertHealth("/q/health/live");
    }

    @Test
    void readinessIsUp() {
        assertHealth("/q/health/ready");
    }

    private void assertHealth(String path) {
        given()
                .when().get(path)
                .then().statusCode(200)
                .body("status", equalTo("UP"));
    }
}
