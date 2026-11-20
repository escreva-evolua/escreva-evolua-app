package br.com.escrevaevolua.identity.adapter.cognito;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeliveryMediumType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.escrevaevolua.identity.application.IdentityOperationException;
import br.com.escrevaevolua.identity.application.RegistrationProvisioner;
import br.com.escrevaevolua.identity.domain.EmailAddress;

@Component
public class CognitoRegistrationProvisioner implements RegistrationProvisioner {
    private static final Logger LOG = LoggerFactory.getLogger(CognitoRegistrationProvisioner.class);


    private final CognitoIdentityProviderClient cognito;
    private final Optional<String> userPoolId;

    @Autowired
    public CognitoRegistrationProvisioner(
            CognitoIdentityProviderClient cognito,
            @Value("${identity.cognito.user-pool-id:}") Optional<String> userPoolId) {
        this.cognito = cognito;
        this.userPoolId = userPoolId;
    }

    @Override
    public void createInvitedUser(UUID invitationId, EmailAddress email) {
        String poolId = userPoolId.orElseThrow(IdentityOperationException::rejected);
        String username = "invite-" + invitationId.toString().replace("-", "");
        try {
            cognito.adminCreateUser(AdminCreateUserRequest.builder()
                    .userPoolId(poolId)
                    .username(username)
                    .userAttributes(
                            AttributeType.builder().name("email").value(email.value()).build(),
                            AttributeType.builder().name("email_verified").value("false").build())
                    .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                    .build());
        } catch (UsernameExistsException exception) {
            if (existingUserHasSameEmail(poolId, username, email)) {
                return;
            }
            throw IdentityOperationException.rejected();
        } catch (RuntimeException exception) {
            LOG.error("Cognito invited-user provisioning failed ({})", exception.getClass().getSimpleName());
            throw IdentityOperationException.internalFailure();
        }
    }

    private boolean existingUserHasSameEmail(String poolId, String username, EmailAddress email) {
        try {
            return cognito.adminGetUser(AdminGetUserRequest.builder()
                            .userPoolId(poolId)
                            .username(username)
                            .build())
                    .userAttributes().stream()
                    .anyMatch(attribute -> "email".equals(attribute.name()) && email.value().equals(attribute.value()));
        } catch (RuntimeException exception) {
            LOG.error("Cognito idempotency lookup failed ({})", exception.getClass().getSimpleName());
            return false;
        }
    }
}