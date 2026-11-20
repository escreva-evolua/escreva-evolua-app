package br.com.escrevaevolua.identity.adapter.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;

import tools.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.escrevaevolua.identity.application.IdentityOperationException;
import br.com.escrevaevolua.identity.application.CognitoUserInfoProvider;

@Component
public class CognitoUserInfoClient implements CognitoUserInfoProvider {
    private static final Logger LOG = LoggerFactory.getLogger(CognitoUserInfoClient.class);


    private final Optional<String> userInfoUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public CognitoUserInfoClient(
            @Value("${identity.cognito.user-info-url:}") Optional<String> userInfoUrl,
            ObjectMapper objectMapper) {
        this.userInfoUrl = userInfoUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @Override
    public UserInfo get(String accessToken) {
        try {
            URI endpoint = endpoint(userInfoUrl.orElseThrow(IdentityOperationException::rejected));
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                    .timeout(Duration.ofSeconds(5))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw IdentityOperationException.rejected();
            }
            UserInfo userInfo = objectMapper.readValue(response.body(), UserInfo.class);
            if (userInfo.sub() == null || userInfo.email() == null) {
                throw IdentityOperationException.rejected();
            }
            return userInfo;
        } catch (IdentityOperationException exception) {
            throw exception;
        } catch (IOException | InterruptedException | IllegalArgumentException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOG.error("Cognito UserInfo request failed ({})", exception.getClass().getSimpleName());
            throw IdentityOperationException.internalFailure();
        }
    }

    static URI endpoint(String value) {
        URI endpoint = URI.create(value);
        if (!"https".equalsIgnoreCase(endpoint.getScheme()) || endpoint.getHost() == null
                || endpoint.getUserInfo() != null || endpoint.getFragment() != null) {
            throw IdentityOperationException.rejected();
        }
        return endpoint;
    }

}
