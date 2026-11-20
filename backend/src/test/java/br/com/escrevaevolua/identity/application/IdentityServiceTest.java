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
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentityServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-30T12:00:00Z");
    private static final String CODE = "invite-code";

    @Test
    void validatesInvitationWithoutRevealingFailureReason() {
        InMemoryIdentityRepository repository = new InMemoryIdentityRepository(invitation());
        IdentityService service = service(repository);

        service.validateInvitation("adult@example.com", CODE);
        IdentityOperationException failure = assertThrows(IdentityOperationException.class,
                () -> service.validateInvitation("other@example.com", CODE));
        assertEquals("Não foi possível concluir a operação de identidade.", failure.getMessage());
    }

    @Test
    void refusesExpiredRevokedAndRedeemedInvitations() {
        String expiredCode = "expired-code";
        String revokedCode = "revoked-code";
        String redeemedCode = "redeemed-code";
        Invitation expired = new Invitation(UUID.randomUUID(), new EmailAddress("adult@example.com"),
                InvitationCode.sha256(expiredCode), Invitation.Status.PENDING, NOW.minusSeconds(1), null, NOW, NOW);
        Invitation revoked = new Invitation(UUID.randomUUID(), new EmailAddress("adult@example.com"),
                InvitationCode.sha256(revokedCode), Invitation.Status.REVOKED, NOW.plusSeconds(60), null, NOW, NOW);
        Invitation redeemed = new Invitation(UUID.randomUUID(), new EmailAddress("adult@example.com"),
                InvitationCode.sha256(redeemedCode), Invitation.Status.REDEEMED, NOW.plusSeconds(60), NOW, NOW, NOW);

        for (Invitation invitation : java.util.List.of(expired, revoked, redeemed)) {
            assertThrows(IdentityOperationException.class,
                    () -> service(new InMemoryIdentityRepository(invitation))
                            .validateInvitation("adult@example.com", switch (invitation.status()) {
                                case PENDING -> expiredCode;
                                case REVOKED -> revokedCode;
                                case REDEEMED -> redeemedCode;
                                case EXPIRED -> throw new AssertionError();
                            }));
        }
    }

    @Test
    void finalizesVerifiedRegistrationAndAllowsSafeRetry() {
        InMemoryIdentityRepository repository = new InMemoryIdentityRepository(invitation());
        IdentityService service = service(repository);
        ExternalIdentity identity = identity();

        IdentityRepository.RegistrationResult created = service.finalizeRegistration(
                identity, "ADULT@example.com", true, " Ana ", CODE);
        IdentityRepository.RegistrationResult retried = service.finalizeRegistration(
                identity, "adult@example.com", true, "Different name", CODE);

        assertEquals(Account.Status.ACTIVE, created.account().status());
        assertEquals("Ana", created.profile().displayName());
        assertEquals(created.account(), retried.account());
        assertEquals(created.profile(), retried.profile());
        assertEquals(Invitation.Status.REDEEMED, repository.invitation.status());
    }

    @Test
    void rejectsUnverifiedEmailAndInactiveAccount() {
        InMemoryIdentityRepository repository = new InMemoryIdentityRepository(invitation());
        IdentityService service = service(repository);

        assertThrows(IdentityOperationException.class,
                () -> service.finalizeRegistration(identity(), "adult@example.com", false, "Ana", CODE));

        IdentityRepository.RegistrationResult created = service.finalizeRegistration(
                identity(), "adult@example.com", true, "Ana", CODE);
        repository.accounts.put(identity(), created.account().block(NOW));
        assertThrows(IdentityOperationException.class, () -> service.getMyAccount(identity()));
    }

    @Test
    void invitationCodesUseThirtyTwoRandomBytesAndPersistOnlyDigest() {
        InvitationCode.Generated generated = InvitationCode.generate();

        assertEquals(43, generated.code().length());
        assertEquals(64, generated.sha256Hex().length());
        assertTrue(generated.code().matches("[A-Za-z0-9_-]+"));
        assertEquals(InvitationCode.sha256(generated.code()), generated.sha256Hex());
    }

    @Test
    void startsCognitoProvisioningOnlyForAnEligibleInvitation() {
        InMemoryIdentityRepository repository = new InMemoryIdentityRepository(invitation());
        IdentityService service = service(repository);
        var invitations = new java.util.ArrayList<UUID>();

        service.startRegistration("adult@example.com", CODE, (invitationId, email) -> {
            invitations.add(invitationId);
            assertEquals("adult@example.com", email.value());
        });

        assertEquals(java.util.List.of(repository.invitation.id()), invitations);
        assertThrows(IdentityOperationException.class,
                () -> service.startRegistration("other@example.com", CODE,
                        (invitationId, email) -> { throw new AssertionError("Provisionamento inesperado"); }));
    }

    @Test
    void leavesInvitationPendingWhenCognitoProvisioningFails() {
        InMemoryIdentityRepository repository = new InMemoryIdentityRepository(invitation());
        IdentityOperationException failure = assertThrows(IdentityOperationException.class,
                () -> service(repository).startRegistration("adult@example.com", CODE,
                        (invitationId, email) -> { throw IdentityOperationException.internalFailure(); }));

        assertTrue(failure.isInternalFailure());
        assertEquals(Invitation.Status.PENDING, repository.invitation.status());
    }

    @Test
    void returnsSameGenericResponseForAnEmailWithAnExistingAccount() {
        InMemoryIdentityRepository repository = new InMemoryIdentityRepository(invitation());
        Account existing = new Account(UUID.randomUUID(), identity(), new EmailAddress("adult@example.com"),
                Account.Status.ACTIVE, NOW, NOW);
        repository.accounts.put(existing.identity(), existing);

        service(repository).startRegistration("adult@example.com", CODE,
                (invitationId, email) -> { throw new AssertionError("Conta existente não pode ser provisionada"); });
        assertEquals(Account.Status.ACTIVE, repository.findAccountByEmail(existing.email()).orElseThrow().status());
    }

    private static IdentityService service(InMemoryIdentityRepository repository) {
        return new IdentityService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static Invitation invitation() {
        return new Invitation(UUID.randomUUID(), new EmailAddress("adult@example.com"), InvitationCode.sha256(CODE),
                Invitation.Status.PENDING, NOW.plusSeconds(86400), null, NOW, NOW);
    }

    private static ExternalIdentity identity() {
        return new ExternalIdentity("https://issuer.example/pool", "subject");
    }

    private static final class InMemoryIdentityRepository implements IdentityRepository {
        private final Map<ExternalIdentity, Account> accounts = new HashMap<>();
        private final Map<UUID, ResponsibleProfile> profiles = new HashMap<>();
        private Invitation invitation;

        private InMemoryIdentityRepository(Invitation invitation) {
            this.invitation = invitation;
        }

        @Override
        public Optional<Invitation> findInvitationByCodeHash(String codeHash) {
            return invitation.codeHash().equals(codeHash) ? Optional.of(invitation) : Optional.empty();
        }

        @Override
        public Optional<Account> findAccountByIdentity(ExternalIdentity identity) {
            return Optional.ofNullable(accounts.get(identity));
        }

        @Override
        public Optional<Account> findAccountByEmail(EmailAddress email) {
            return accounts.values().stream().filter(account -> account.email().equals(email)).findFirst();
        }

        @Override
        public Optional<Account> findAccountById(UUID accountId) {
            return accounts.values().stream().filter(account -> account.id().equals(accountId)).findFirst();
        }

        @Override
        public Optional<ResponsibleProfile> findResponsibleProfileByAccountId(UUID accountId) {
            return Optional.ofNullable(profiles.get(accountId));
        }

        @Override
        public Invitation createInvitation(Invitation invitation) {
            this.invitation = invitation;
            return invitation;
        }

        @Override
        public boolean revokeInvitation(UUID invitationId, Instant now) {
            if (!invitation.id().equals(invitationId) || invitation.statusAt(now) != Invitation.Status.PENDING) {
                return false;
            }
            invitation = invitation.revoke(now);
            return true;
        }

        @Override
        public boolean transitionAccount(UUID accountId, Account.Status expected, Account.Status target,
                String actorId, Instant now) {
            Account account = findAccountById(accountId).orElse(null);
            if (account == null || account.status() != expected) {
                return false;
            }
            accounts.put(account.identity(), new Account(account.id(), account.identity(), account.email(), target,
                    account.createdAt(), now));
            return true;
        }

        @Override
        public RegistrationResult finalizeRegistration(UUID invitationId, String expectedCodeHash,
                Account account, ResponsibleProfile profile, Instant now) {
            if (!invitation.id().equals(invitationId) || !invitation.matches(account.email(), expectedCodeHash, now)) {
                throw IdentityOperationException.rejected();
            }
            invitation = invitation.redeem(now);
            Account activeAccount = account.activate(now);
            accounts.put(activeAccount.identity(), activeAccount);
            profiles.put(profile.accountId(), profile);
            return new RegistrationResult(activeAccount, profile);
        }
    }
}
