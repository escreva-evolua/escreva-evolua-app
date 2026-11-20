package br.com.escrevaevolua.identity.adapter.http;

import org.junit.jupiter.api.Test;

import br.com.escrevaevolua.identity.application.IdentityOperationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CognitoUserInfoClientTest {

    @Test
    void onlyAllowsHttpsUserInfoEndpointsWithoutEmbeddedCredentials() {
        assertEquals("https", CognitoUserInfoClient.endpoint("https://cognito.example/oauth2/userInfo").getScheme());
        assertThrows(IdentityOperationException.class,
                () -> CognitoUserInfoClient.endpoint("http://cognito.example/oauth2/userInfo"));
        assertThrows(IdentityOperationException.class,
                () -> CognitoUserInfoClient.endpoint("https://user:password@cognito.example/userInfo"));
    }
}