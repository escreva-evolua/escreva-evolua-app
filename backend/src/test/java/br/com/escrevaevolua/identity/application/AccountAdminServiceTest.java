package br.com.escrevaevolua.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escrevaevolua.identity.domain.Account;
import br.com.escrevaevolua.identity.domain.EmailAddress;
import br.com.escrevaevolua.identity.domain.ExternalIdentity;
import br.com.escrevaevolua.identity.domain.Invitation;
import br.com.escrevaevolua.identity.domain.ResponsibleProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountAdminServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-30T12:00:00Z");

    @Test
    void appliesOnlyApprovedLifecycleTransitionsAndAuditsOperator() {
        AccountRepository repository = new AccountRepository();
        Account account = new Account(UUID.randomUUID(), new ExternalIdentity("https://issuer.example/pool", "sub"),
                new EmailAddress("adult@example.com"), Account.Status.ACTIVE, NOW, NOW);
        repository.accounts.put(account.id(), account);
        AccountAdminService service = new AccountAdminService(repository, Clock.fixed(NOW, ZoneOffset.UTC));

        service.transition(account.id(), AccountAdminService.Action.BLOCK, "operator-1");
        assertEquals(Account.Status.BLOCKED, repository.findAccountById(account.id()).orElseThrow().status());
        service.transition(account.id(), AccountAdminService.Action.UNBLOCK, "operator-1");
        service.transition(account.id(), AccountAdminService.Action.DISABLE, "operator-1");
        assertEquals(Account.Status.DISABLED, repository.findAccountById(account.id()).orElseThrow().status());
        assertEquals("operator-1", repository.lastActor);
        assertThrows(IdentityOperationException.class,
                () -> service.transition(account.id(), AccountAdminService.Action.UNBLOCK, "operator-1"));
        assertThrows(IdentityOperationException.class,
                () -> service.transition(account.id(), AccountAdminService.Action.BLOCK, " "));
    }

    private static final class AccountRepository implements IdentityRepository {
        private final Map<UUID, Account> accounts = new HashMap<>();
        private String lastActor;

        @Override
        public Optional<Invitation> findInvitationByCodeHash(String codeHash) { return Optional.empty(); }
        @Override
        public Optional<Account> findAccountByIdentity(ExternalIdentity identity) { return Optional.empty(); }
        @Override
        public Optional<Account> findAccountByEmail(EmailAddress email) { return Optional.empty(); }
        @Override
        public Optional<Account> findAccountById(UUID accountId) { return Optional.ofNullable(accounts.get(accountId)); }
        @Override
        public Optional<ResponsibleProfile> findResponsibleProfileByAccountId(UUID accountId) { return Optional.empty(); }
        @Override
        public Invitation createInvitation(Invitation invitation) { throw new UnsupportedOperationException(); }
        @Override
        public boolean revokeInvitation(UUID invitationId, Instant now) { return false; }
        @Override
        public boolean transitionAccount(UUID accountId, Account.Status expected, Account.Status target,
                String actorId, Instant now) {
            Account current = accounts.get(accountId);
            if (current == null || current.status() != expected) {
                return false;
            }
            accounts.put(accountId, new Account(current.id(), current.identity(), current.email(), target,
                    current.createdAt(), now));
            lastActor = actorId;
            return true;
        }
        @Override
        public RegistrationResult finalizeRegistration(UUID invitationId, String expectedCodeHash,
                Account account, ResponsibleProfile profile, Instant now) { throw new UnsupportedOperationException(); }
    }
}