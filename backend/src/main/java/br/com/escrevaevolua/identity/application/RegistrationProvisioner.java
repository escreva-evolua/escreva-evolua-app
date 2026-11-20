package br.com.escrevaevolua.identity.application;

import java.util.UUID;

import br.com.escrevaevolua.identity.domain.EmailAddress;

public interface RegistrationProvisioner {

    void createInvitedUser(UUID invitationId, EmailAddress email);
}