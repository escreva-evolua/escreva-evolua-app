package br.com.escrevaevolua.identity.adapter.http;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import br.com.escrevaevolua.identity.domain.ExternalIdentity;

@Component
public class ValidatedAccessToken {
    private final Optional<String> expectedIssuer;
    private final Optional<String> expectedClientId;
    private final Optional<String> expectedAudience;

    public ValidatedAccessToken(@Value("${identity.cognito.issuer:}") String issuer,
            @Value("${identity.cognito.client-id:}") String clientId,
            @Value("${identity.cognito.audience:}") String audience) {
        this.expectedIssuer = optional(issuer);
        this.expectedClientId = optional(clientId);
        this.expectedAudience = optional(audience);
    }

    public ExternalIdentity require(Jwt token) {
        if (token == null || expectedIssuer.isEmpty() || expectedClientId.isEmpty()
                || !expectedIssuer.get().equals(token.getIssuer() == null ? null : token.getIssuer().toString())
                || !"access".equals(token.getClaimAsString("token_use"))
                || !expectedClientId.get().equals(token.getClaimAsString("client_id"))
                || !audienceMatches(token.getAudience())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        try {
            return new ExternalIdentity(expectedIssuer.get(), token.getSubject());
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean audienceMatches(java.util.List<String> audiences) {
        if (expectedAudience.isEmpty()) return true;
        return audiences != null && audiences.contains(expectedAudience.get());
    }

    private static Optional<String> optional(String value) {
        return Optional.ofNullable(value).filter(candidate -> !candidate.isBlank());
    }
}
