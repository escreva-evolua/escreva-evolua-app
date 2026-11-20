package br.com.escrevaevolua.identity.adapter.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import br.com.escrevaevolua.identity.application.CognitoUserInfoProvider;
import br.com.escrevaevolua.identity.application.IdentityOperationException;
import br.com.escrevaevolua.identity.application.IdentityRepository;
import br.com.escrevaevolua.identity.application.IdentityService;
import br.com.escrevaevolua.identity.application.RegistrationProvisioner;
import br.com.escrevaevolua.identity.domain.Account;
import br.com.escrevaevolua.identity.domain.EmailAddress;
import br.com.escrevaevolua.identity.domain.ExternalIdentity;
import br.com.escrevaevolua.identity.domain.ResponsibleProfile;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "identity.cognito.issuer=https://server.example.com", "identity.cognito.client-id=test-client",
        "app.cors.origins=http://localhost:3000" })
class IdentityOidcResourceTest {
    @LocalServerPort int port;
    @MockitoBean JwtDecoder jwtDecoder;
    @MockitoBean IdentityService identityService;
    @MockitoBean CognitoUserInfoProvider userInfoProvider;
    @MockitoBean RegistrationProvisioner registrationProvisioner;

    @BeforeEach
    void configureDecoder() {
        when(jwtDecoder.decode(anyString())).thenAnswer(invocation -> token(invocation.getArgument(0).toString()));
    }

    @Test
    void protectedAccountEndpointRejectsAnonymousRequest() throws Exception {
        assertEquals(401, get("/api/v1/auth/me", null).statusCode());
    }

    @Test
    void allowsCorsPreflightOnlyForConfiguredFrontendOrigin() throws Exception {
        HttpResponse<String> allowed = preflight("http://localhost:3000");
        assertEquals(200, allowed.statusCode());
        assertEquals("http://localhost:3000", allowed.headers().firstValue("Access-Control-Allow-Origin").orElseThrow());
        assertEquals(403, preflight("https://attacker.example").statusCode());
    }

    @Test
    void rejectsIdTokensWrongClientAndWrongIssuer() throws Exception {
        assertEquals(401, get("/api/v1/auth/me", "id-token").statusCode());
        assertEquals(401, get("/api/v1/auth/me", "wrong-client").statusCode());
        assertEquals(401, get("/api/v1/auth/me", "wrong-issuer").statusCode());
    }

    @Test
    void acceptsAccessTokenAndUsesAuthenticatedIdentityForAccountLookup() throws Exception {
        when(identityService.getMyAccount(any())).thenThrow(IdentityOperationException.rejected());
        HttpResponse<String> response = get("/api/v1/auth/me", "valid");
        assertEquals(400, response.statusCode());
        org.junit.jupiter.api.Assertions.assertTrue(response.body().contains("Não foi possível concluir a operação de identidade."));
    }

    @Test
    void returnsTheAuthenticatedAccountContract() throws Exception {
        var account = account();
        var profile = new ResponsibleProfile(UUID.randomUUID(), account.id(), "Ana", account.createdAt(), account.updatedAt());
        when(identityService.getMyAccount(any())).thenReturn(new IdentityRepository.AccountView(account, profile));

        HttpResponse<String> response = get("/api/v1/auth/me", "valid");

        assertEquals(200, response.statusCode());
        org.junit.jupiter.api.Assertions.assertTrue(response.body().contains("\"email\":\"adult@example.com\""));
        org.junit.jupiter.api.Assertions.assertTrue(response.body().contains("\"status\":\"ACTIVE\""));
        org.junit.jupiter.api.Assertions.assertTrue(response.body().contains("\"displayName\":\"Ana\""));
        verify(identityService).getMyAccount(new ExternalIdentity("https://server.example.com", "subject"));
    }

    @Test
    void validatesInvitationThroughTheHttpContract() throws Exception {
        HttpResponse<String> response = post("/api/v1/auth/invitations/validate",
                "{\"email\":\"adult@example.com\",\"code\":\"invite-code\"}", null);

        assertEquals(204, response.statusCode());
        verify(identityService).validateInvitation("adult@example.com", "invite-code");
    }

    @Test
    void startsRegistrationWithGenericAcceptedResponse() throws Exception {
        HttpResponse<String> response = post("/api/v1/auth/register",
                "{\"email\":\"adult@example.com\",\"code\":\"invite-code\"}", null);

        assertEquals(202, response.statusCode());
        org.junit.jupiter.api.Assertions.assertTrue(response.body().contains("Se o convite for válido"));
        org.junit.jupiter.api.Assertions.assertFalse(response.body().contains("adult@example.com"));
        verify(identityService).startRegistration("adult@example.com", "invite-code", registrationProvisioner);
    }

    @Test
    void finalizesRegistrationUsingUserInfoAndReturnsAccountContract() throws Exception {
        var account = account();
        var profile = new ResponsibleProfile(UUID.randomUUID(), account.id(), "Ana", account.createdAt(), account.updatedAt());
        when(userInfoProvider.get("finalize-token"))
                .thenReturn(new CognitoUserInfoProvider.UserInfo("subject", "adult@example.com", true));
        when(identityService.finalizeRegistration(any(), anyString(), org.mockito.ArgumentMatchers.eq(true),
                anyString(), anyString())).thenReturn(new IdentityRepository.RegistrationResult(account, profile));

        HttpResponse<String> response = post("/api/v1/auth/register/finalize",
                "{\"code\":\"invite-code\",\"displayName\":\"Ana\"}", "finalize-token");

        assertEquals(200, response.statusCode());
        org.junit.jupiter.api.Assertions.assertTrue(response.body().contains("\"email\":\"adult@example.com\""));
        org.junit.jupiter.api.Assertions.assertTrue(response.body().contains("\"displayName\":\"Ana\""));
        verify(userInfoProvider).get("finalize-token");
        verify(identityService).finalizeRegistration(new ExternalIdentity("https://server.example.com", "subject"),
                "adult@example.com", true, "Ana", "invite-code");
    }

    @Test
    void mapsRegistrationFailureToTheGenericHttpError() throws Exception {
        doThrow(IdentityOperationException.internalFailure()).when(identityService)
                .startRegistration(anyString(), anyString(), any(RegistrationProvisioner.class));

        HttpResponse<String> response = post("/api/v1/auth/register",
                "{\"email\":\"adult@example.com\",\"code\":\"invite-code\"}", null);

        assertEquals(500, response.statusCode());
        org.junit.jupiter.api.Assertions.assertFalse(response.body().contains("adult@example.com"));
        org.junit.jupiter.api.Assertions.assertFalse(response.body().contains("invite-code"));
    }

    private HttpResponse<String> get(String path, String bearer) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url(path))).GET();
        if (bearer != null) request.header("Authorization", "Bearer " + bearer);
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body, String bearer) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (bearer != null) request.header("Authorization", "Bearer " + bearer);
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static Account account() {
        Instant now = Instant.parse("2026-10-30T12:00:00Z");
        return new Account(UUID.randomUUID(), new ExternalIdentity("https://server.example.com", "subject"),
                new EmailAddress("adult@example.com"), Account.Status.ACTIVE, now, now);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpResponse<String> preflight(String origin) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url("/api/v1/auth/register")))
                .header("Origin", origin)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "authorization,content-type")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static Jwt token(String marker) {
        String issuer = marker.equals("wrong-issuer") ? "https://wrong.example" : "https://server.example.com";
        String use = marker.equals("id-token") ? "id" : "access";
        String client = marker.equals("wrong-client") ? "other-client" : "test-client";
        Instant now = Instant.now();
        return new Jwt(marker, now, now.plusSeconds(120), Map.of("alg", "RS256"), Map.of("iss", issuer,
                "sub", "subject", "token_use", use, "client_id", client, "aud", List.of()));
    }
}
