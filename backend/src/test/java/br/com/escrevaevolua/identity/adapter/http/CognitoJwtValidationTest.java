package br.com.escrevaevolua.identity.adapter.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;

class CognitoJwtValidationTest {
    private static final String ISSUER = "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_test";
    private static final String CLIENT_ID = "client-123";
    private static RSAKey signingKey;
    private static JwtEncoder encoder;
    private static JwtDecoder decoder;

    @BeforeAll
    static void configure() throws Exception {
        signingKey = new RSAKeyGenerator(2048).keyID("test-key").generate();
        encoder = new NimbusJwtEncoder(new com.nimbusds.jose.jwk.source.ImmutableJWKSet<>(new JWKSet(signingKey)));
        NimbusJwtDecoder nimbus = NimbusJwtDecoder.withPublicKey(signingKey.toRSAPublicKey()).build();
        nimbus.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(),
                new JwtIssuerValidator(ISSUER), SecurityConfiguration.cognitoClaimsValidator(CLIENT_ID, "")));
        decoder = nimbus;
    }

    @Test
    void acceptsCryptographicallySignedCognitoAccessToken() {
        Jwt decoded = decoder.decode(token("access", CLIENT_ID, ISSUER, Instant.now().plusSeconds(120), signingKey));
        assertEquals("subject-1", decoded.getSubject());
    }

    @Test
    void rejectsIdTokenWrongClientWrongIssuerExpiredAndInvalidSignature() throws Exception {
        assertThrows(JwtException.class, () -> decode("id", CLIENT_ID, ISSUER,
                Instant.now().plusSeconds(120), signingKey));
        assertThrows(JwtException.class, () -> decode("access", "other-client", ISSUER,
                Instant.now().plusSeconds(120), signingKey));
        assertThrows(JwtException.class, () -> decode("access", CLIENT_ID, "https://wrong.example",
                Instant.now().plusSeconds(120), signingKey));
        assertThrows(JwtException.class, () -> decode("access", CLIENT_ID, ISSUER,
                Instant.now().minusSeconds(120), signingKey));
        RSAKey otherKey = new RSAKeyGenerator(2048).keyID("test-key").generate();
        assertThrows(JwtException.class, () -> decode("access", CLIENT_ID, ISSUER,
                Instant.now().plusSeconds(120), otherKey));
    }

    @Test
    void rejectsMissingSubject() {
        Jwt tokenWithoutSubject = Jwt.withTokenValue("missing-subject")
                .header("alg", "RS256").issuer(ISSUER)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(120))
                .claim("token_use", "access").claim("client_id", CLIENT_ID).build();

        assertTrue(SecurityConfiguration.cognitoClaimsValidator(CLIENT_ID, "")
                .validate(tokenWithoutSubject).hasErrors());
    }

    @Test
    void requiresConfiguredResourceAudience() {
        Jwt tokenWithWrongAudience = Jwt.withTokenValue("wrong-audience")
                .header("alg", "RS256").issuer(ISSUER).subject("subject-1")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(120))
                .audience(java.util.List.of("other-resource"))
                .claim("token_use", "access").claim("client_id", CLIENT_ID).build();

        assertTrue(SecurityConfiguration.cognitoClaimsValidator(CLIENT_ID, "api-resource")
                .validate(tokenWithWrongAudience).hasErrors());
    }

    private static Jwt decode(String tokenUse, String clientId, String issuer, Instant expiresAt, RSAKey key) {
        JwtEncoder selectedEncoder = key.equals(signingKey) ? encoder
                : new NimbusJwtEncoder(new com.nimbusds.jose.jwk.source.ImmutableJWKSet<>(new JWKSet(key)));
        String signed = token(tokenUse, clientId, issuer, expiresAt, selectedEncoder);
        return decoder.decode(signed);
    }

    private static String token(String tokenUse, String clientId, String issuer, Instant expiresAt, RSAKey key) {
        return token(tokenUse, clientId, issuer, expiresAt, key.equals(signingKey) ? encoder
                : new NimbusJwtEncoder(new com.nimbusds.jose.jwk.source.ImmutableJWKSet<>(new JWKSet(key))));
    }

    private static String token(String tokenUse, String clientId, String issuer, Instant expiresAt, JwtEncoder signer) {
        Instant now = expiresAt.isBefore(Instant.now()) ? expiresAt.minusSeconds(180) : Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).subject("subject-1")
                .issuedAt(now).expiresAt(expiresAt)
                .claim("token_use", tokenUse).claim("client_id", clientId).build();
        return signer.encode(JwtEncoderParameters.from(
                org.springframework.security.oauth2.jwt.JwsHeader.with(SignatureAlgorithm.RS256)
                        .keyId("test-key").build(), claims)).getTokenValue();
    }
}
