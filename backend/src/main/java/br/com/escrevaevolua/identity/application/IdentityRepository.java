package br.com.escrevaevolua.identity.application;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import br.com.escrevaevolua.identity.domain.Account;
import br.com.escrevaevolua.identity.domain.EmailAddress;
import br.com.escrevaevolua.identity.domain.ExternalIdentity;
import br.com.escrevaevolua.identity.domain.Invitation;
import br.com.escrevaevolua.identity.domain.ResponsibleProfile;

public interface IdentityRepository {

    Optional<Invitation> findInvitationByCodeHash(String codeHash);

    Optional<Account> findAccountByIdentity(ExternalIdentity identity);

    Optional<Account> findAccountByEmail(EmailAddress email);

    Optional<Account> findAccountById(UUID accountId);

    Optional<ResponsibleProfile> findResponsibleProfileByAccountId(UUID accountId);

    Invitation createInvitation(Invitation invitation);

    boolean revokeInvitation(UUID invitationId, Instant now);

        boolean transitionAccount(UUID accountId, Account.Status expected, Account.Status target,
            String actorId, Instant now);

    RegistrationResult finalizeRegistration(
            UUID invitationId,
            String expectedCodeHash,
            Account account,
            ResponsibleProfile profile,
            Instant now);

    record RegistrationResult(Account account, ResponsibleProfile profile) {
    }

    record AccountView(Account account, ResponsibleProfile responsibleProfile) {
        public AccountView {
            if (!account.id().equals(responsibleProfile.accountId())) {
                throw new IllegalArgumentException("Perfil não pertence à conta");
            }
        }
    }
}