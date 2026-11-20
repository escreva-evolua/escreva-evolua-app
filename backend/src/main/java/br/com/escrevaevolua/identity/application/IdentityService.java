package br.com.escrevaevolua.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


import br.com.escrevaevolua.identity.domain.Account;
import br.com.escrevaevolua.identity.domain.EmailAddress;
import br.com.escrevaevolua.identity.domain.ExternalIdentity;
import br.com.escrevaevolua.identity.domain.Invitation;
import br.com.escrevaevolua.identity.domain.ResponsibleProfile;

public class IdentityService {

    private final IdentityRepository repository;
    private final Clock clock;

    public IdentityService(IdentityRepository repository) {
        this(repository, Clock.systemUTC());
    }

    public IdentityService(IdentityRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
    }

    public void validateInvitation(String emailValue, String code) {
        requireInvitation(email(emailValue), codeHash(code));
    }

    public void startRegistration(String emailValue, String code, RegistrationProvisioner provisioner) {
        EmailAddress email = email(emailValue);
        String codeHash = codeHash(code);
        Invitation invitation = requireInvitation(email, codeHash);
        if (repository.findAccountByEmail(email).isPresent()) {
            return;
        }
        provisioner.createInvitedUser(invitation.id(), email);
    }

    public IdentityRepository.RegistrationResult finalizeRegistration(
            ExternalIdentity identity,
            String verifiedEmail,
            boolean emailVerified,
            String displayName,
            String code) {
        Objects.requireNonNull(identity);
        EmailAddress email = email(verifiedEmail);
        if (!emailVerified) {
            throw IdentityOperationException.rejected();
        }

        var existingAccount = repository.findAccountByIdentity(identity);
        if (existingAccount.isPresent()) {
            Account account = existingAccount.get();
            if (!account.email().equals(email) || account.status() == Account.Status.BLOCKED
                    || account.status() == Account.Status.DISABLED) {
                throw IdentityOperationException.rejected();
            }
            if (account.isActive()) {
                ResponsibleProfile profile = repository.findResponsibleProfileByAccountId(account.id())
                        .orElseThrow(IdentityOperationException::rejected);
                return new IdentityRepository.RegistrationResult(account, profile);
            }
        }

        String codeHash = codeHash(code);
        Invitation invitation = repository.findInvitationByCodeHash(codeHash)
                .orElseThrow(IdentityOperationException::rejected);
        Instant now = clock.instant();
        boolean retryingConsumedInvitation = existingAccount.isPresent()
            && existingAccount.get().status() == Account.Status.PENDING_ACTIVATION
            && invitation.status() == Invitation.Status.REDEEMED
            && invitation.email().equals(email)
            && invitation.codeHash().equals(codeHash);
        if (!retryingConsumedInvitation && !invitation.matches(email, codeHash, now)) {
            throw IdentityOperationException.rejected();
        }

        Account account = existingAccount.orElseGet(() -> new Account(UUID.randomUUID(), identity, email,
            Account.Status.PENDING_ACTIVATION, now, now));
        ResponsibleProfile profile = repository.findResponsibleProfileByAccountId(account.id())
            .orElseGet(() -> new ResponsibleProfile(UUID.randomUUID(), account.id(), displayName(displayName), now, now));
        return repository.finalizeRegistration(invitation.id(), codeHash, account, profile, now);
    }

    public IdentityRepository.AccountView getMyAccount(ExternalIdentity identity) {
        Objects.requireNonNull(identity);
        Account account = repository.findAccountByIdentity(identity)
                .filter(Account::isActive)
                .orElseThrow(IdentityOperationException::rejected);
        ResponsibleProfile profile = repository.findResponsibleProfileByAccountId(account.id())
                .orElseThrow(IdentityOperationException::rejected);
        return new IdentityRepository.AccountView(account, profile);
    }

    private static EmailAddress email(String value) {
        try {
            return new EmailAddress(value);
        } catch (IllegalArgumentException exception) {
            throw IdentityOperationException.rejected();
        }
    }

    private static String displayName(String value) {
        if (value == null || value.isBlank()) {
            throw IdentityOperationException.rejected();
        }
        return value.strip();
    }

    private static String codeHash(String code) {
        try {
            return InvitationCode.sha256(code);
        } catch (IllegalArgumentException exception) {
            throw IdentityOperationException.rejected();
        }
    }

    private Invitation requireInvitation(EmailAddress email, String codeHash) {
        Invitation invitation = repository.findInvitationByCodeHash(codeHash)
                .orElseThrow(IdentityOperationException::rejected);
        if (!invitation.matches(email, codeHash, clock.instant())) {
            throw IdentityOperationException.rejected();
        }
        return invitation;
    }
}