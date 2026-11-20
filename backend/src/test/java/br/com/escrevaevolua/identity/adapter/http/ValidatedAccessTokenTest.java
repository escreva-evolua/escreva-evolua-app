package br.com.escrevaevolua.identity.adapter.http;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidatedAccessTokenTest {

    private final ValidatedAccessToken validator = new ValidatedAccessToken(
            "https://issuer.example/pool", "public-client", "api-resource");

    @Test
    void acceptsOnlyAccessTokenFromExpectedIssuerAndClient() {
        assertEquals("subject", validator.require(jwt("https://issuer.example/pool", "subject", "access", "public-client", List.of("api-resource"))).subject());
        assertThrows(ResponseStatusException.class, () -> validator.require(jwt("https://issuer.example/pool", "subject", "id", "public-client", List.of("api-resource"))));
        assertThrows(ResponseStatusException.class, () -> validator.require(jwt("https://issuer.example/pool", "subject", "access", "other-client", List.of("api-resource"))));
        assertThrows(ResponseStatusException.class, () -> validator.require(jwt("https://other.example/pool", "subject", "access", "public-client", List.of("api-resource"))));
        assertThrows(ResponseStatusException.class, () -> validator.require(jwt("https://issuer.example/pool", "subject", "access", "public-client", List.of("wrong-audience"))));
    }

    @Test
    void acceptsCognitoAccessTokenWithoutOptionalAudienceWhenResourceBindingIsNotConfigured() {
        ValidatedAccessToken noAudiencePolicy = new ValidatedAccessToken(
                "https://issuer.example/pool", "public-client", "");

        assertEquals("subject", noAudiencePolicy.require(jwt("https://issuer.example/pool", "subject", "access", "public-client", List.of())).subject());
    }

    private static Jwt jwt(String issuer, String subject, String use, String client, List<String> audience) {
        Instant now = Instant.now();
        return new Jwt("token", now, now.plusSeconds(60), Map.of("alg", "none"), Map.of("iss", issuer,
                "sub", subject, "token_use", use, "client_id", client, "aud", audience));
    }
}
