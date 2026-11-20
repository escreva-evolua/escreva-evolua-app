package br.com.escrevaevolua.identity.adapter.http;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escrevaevolua.identity.application.CognitoUserInfoProvider;
import br.com.escrevaevolua.identity.application.IdentityRepository;
import br.com.escrevaevolua.identity.application.IdentityService;
import br.com.escrevaevolua.identity.application.RegistrationProvisioner;
import br.com.escrevaevolua.identity.domain.ExternalIdentity;

@RestController
@RequestMapping(path = "/api/v1/auth", produces = "application/json")
public class IdentityResource {
    private final IdentityService identityService;
    private final ValidatedAccessToken accessToken;
    private final CognitoUserInfoProvider userInfoClient;
    private final RegistrationProvisioner registrationProvisioner;

    public IdentityResource(IdentityService identityService, ValidatedAccessToken accessToken,
            CognitoUserInfoProvider userInfoClient, RegistrationProvisioner registrationProvisioner) {
        this.identityService = identityService;
        this.accessToken = accessToken;
        this.userInfoClient = userInfoClient;
        this.registrationProvisioner = registrationProvisioner;
    }

    @PostMapping(path = "/invitations/validate", consumes = "application/json")
    public ResponseEntity<Void> validateInvitation(@RequestBody(required = false) ValidateInvitationRequest request) {
        if (request == null) throw br.com.escrevaevolua.identity.application.IdentityOperationException.rejected();
        identityService.validateInvitation(request.email(), request.code());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/register", consumes = "application/json")
    public ResponseEntity<RegistrationAccepted> startRegistration(@RequestBody(required = false) RegisterRequest request) {
        if (request == null) throw br.com.escrevaevolua.identity.application.IdentityOperationException.rejected();
        identityService.startRegistration(request.email(), request.code(), registrationProvisioner);
        return ResponseEntity.accepted().body(new RegistrationAccepted("Se o convite for válido, enviaremos as instruções ao e-mail informado."));
    }

    @PostMapping(path = "/register/finalize", consumes = "application/json")
    public AccountResponse finalizeRegistration(@RequestBody(required = false) FinalizeRegistrationRequest request,
            @AuthenticationPrincipal Jwt jwt, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        if (request == null) throw br.com.escrevaevolua.identity.application.IdentityOperationException.rejected();
        ExternalIdentity identity = accessToken.require(jwt);
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : "";
        CognitoUserInfoProvider.UserInfo userInfo = userInfoClient.get(token);
        if (!identity.subject().equals(userInfo.sub())) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        var result = identityService.finalizeRegistration(identity, userInfo.email(), userInfo.email_verified(), request.displayName(), request.code());
        return AccountResponse.from(result);
    }

    @GetMapping("/me")
    public AccountResponse getMyAccount(@AuthenticationPrincipal Jwt jwt) {
        return AccountResponse.from(identityService.getMyAccount(accessToken.require(jwt)));
    }

    public record ValidateInvitationRequest(String email, String code) {}
    public record RegisterRequest(String email, String code) {}
    public record FinalizeRegistrationRequest(String code, String displayName) {}
    public record RegistrationAccepted(String message) {}
    public record AccountResponse(UUID id, String email, String status, String displayName) {
        private static AccountResponse from(IdentityRepository.AccountView accountView) {
            return new AccountResponse(accountView.account().id(), accountView.account().email().value(), accountView.account().status().name(), accountView.responsibleProfile().displayName());
        }
        private static AccountResponse from(IdentityRepository.RegistrationResult registration) {
            return new AccountResponse(registration.account().id(), registration.account().email().value(), registration.account().status().name(), registration.profile().displayName());
        }
    }
}
