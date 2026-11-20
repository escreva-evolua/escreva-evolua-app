package br.com.escrevaevolua.identity.adapter.cognito;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeliveryMediumType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

import br.com.escrevaevolua.identity.application.IdentityOperationException;
import br.com.escrevaevolua.identity.domain.EmailAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CognitoRegistrationProvisionerTest {

    @Test
    void createsTechnicalUserAndNeverMarksEmailVerified() {
        AtomicReference<AdminCreateUserRequest> captured = new AtomicReference<>();
        CognitoRegistrationProvisioner provisioner = provisioner((method, request) -> {
            if ("adminCreateUser".equals(method)) {
                captured.set((AdminCreateUserRequest) request);
                return AdminCreateUserResponse.builder().build();
            }
            throw new AssertionError("Unexpected Cognito call: " + method);
        });
        UUID invitationId = UUID.randomUUID();

        provisioner.createInvitedUser(invitationId, new EmailAddress("adult@example.com"));

        AdminCreateUserRequest request = captured.get();
        assertEquals("pool-id", request.userPoolId());
        assertEquals("invite-" + invitationId.toString().replace("-", ""), request.username());
        assertFalse(request.username().contains("@"));
        assertEquals(DeliveryMediumType.EMAIL, request.desiredDeliveryMediums().getFirst());
        assertEquals("adult@example.com", attribute(request, "email"));
        assertEquals("false", attribute(request, "email_verified"));
    }

    @Test
    void retryIsIdempotentOnlyWhenExistingTechnicalUserHasTheSameEmail() {
        CognitoRegistrationProvisioner sameEmail = provisioner((method, request) -> {
            if ("adminCreateUser".equals(method)) {
                throw UsernameExistsException.builder().message("exists").build();
            }
            if ("adminGetUser".equals(method)) {
                return AdminGetUserResponse.builder().userAttributes(
                        AttributeType.builder().name("email").value("adult@example.com").build()).build();
            }
            throw new AssertionError("Unexpected Cognito call: " + method);
        });
        sameEmail.createInvitedUser(UUID.randomUUID(), new EmailAddress("adult@example.com"));

        CognitoRegistrationProvisioner differentEmail = provisioner((method, request) -> {
            if ("adminCreateUser".equals(method)) {
                throw UsernameExistsException.builder().message("exists").build();
            }
            return AdminGetUserResponse.builder().userAttributes(
                    AttributeType.builder().name("email").value("other@example.com").build()).build();
        });
        assertThrows(IdentityOperationException.class,
                () -> differentEmail.createInvitedUser(UUID.randomUUID(), new EmailAddress("adult@example.com")));
    }

    @Test
    void mapsCognitoServiceFailureToGenericInternalFailure() {
        CognitoRegistrationProvisioner provisioner = provisioner((method, request) -> {
            throw new IllegalStateException("synthetic upstream failure");
        });

        IdentityOperationException failure = assertThrows(IdentityOperationException.class,
                () -> provisioner.createInvitedUser(UUID.randomUUID(), new EmailAddress("adult@example.com")));

        assertEquals("Não foi possível concluir a operação de identidade.", failure.getMessage());
        assertTrue(failure.isInternalFailure());
    }

    private static String attribute(AdminCreateUserRequest request, String name) {
        return request.userAttributes().stream().filter(attribute -> name.equals(attribute.name()))
                .map(AttributeType::value).findFirst().orElseThrow();
    }

    private static CognitoRegistrationProvisioner provisioner(SdkCall call) {
        CognitoIdentityProviderClient client = (CognitoIdentityProviderClient) Proxy.newProxyInstance(
                CognitoIdentityProviderClient.class.getClassLoader(),
                new Class<?>[] { CognitoIdentityProviderClient.class },
                (proxy, method, arguments) -> {
                    if (arguments != null && arguments.length > 0) {
                        return call.invoke(method.getName(), arguments[0]);
                    }
                    return switch (method.getName()) {
                        case "serviceName" -> "cognito-idp";
                        case "close" -> null;
                        case "toString" -> "test-cognito-client";
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == arguments[0];
                        default -> throw new AssertionError("Unexpected Cognito call: " + method.getName());
                    };
                });
        return new CognitoRegistrationProvisioner(client, Optional.of("pool-id"));
    }

    @FunctionalInterface
    private interface SdkCall {
        Object invoke(String method, Object request);
    }
}
