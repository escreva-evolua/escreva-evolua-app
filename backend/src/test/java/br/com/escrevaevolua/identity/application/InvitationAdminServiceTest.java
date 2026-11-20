package br.com.escrevaevolua.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escrevaevolua.identity.domain.Account;
import br.com.escrevaevolua.identity.domain.EmailAddress;
import br.com.escrevaevolua.identity.domain.ExternalIdentity;
import br.com.escrevaevolua.identity.domain.Invitation;
import br.com.escrevaevolua.identity.domain.ResponsibleProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvitationAdminServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-30T12:00:00Z");

    @Test
    void createsThirtyDayInvitationAndAuditsWithoutCodeOrEmail() {
        InvitationRepository repository = new InvitationRepository();
        AuditLog audit = new AuditLog();
        InvitationAdminService service = service(repository, audit);

        var created = service.create(" Adult@Example.com ", "operator-1");
        Invitation saved = repository.invitation;

        assertEquals(NOW.plusSeconds(30 * 24 * 60 * 60), created.expiresAt());
        assertEquals(InvitationCode.sha256(created.code()), saved.codeHash());
        assertNotEquals(created.code(), saved.codeHash());
        assertEquals(new EmailAddress("adult@example.com"), saved.email());
        assertEquals("INVITATION_CREATED", audit.events.getFirst().eventType());
        assertEquals("operator-1", audit.events.getFirst().actorId());
        assertTrue(!audit.events.getFirst().subjectId().contains("@"));
    }

    @Test
    void revokesOnlyPendingInvitationsAndRequiresAnAuditableActor() {
        InvitationRepository repository = new InvitationRepository();
        AuditLog audit = new AuditLog();
        InvitationAdminService service = service(repository, audit);
        var created = service.create("adult@example.com", "operator-1");

        service.revoke(created.id(), "operator-2");

        assertEquals(Invitation.Status.REVOKED, repository.invitation.status());
        assertEquals("INVITATION_REVOKED", audit.events.getLast().eventType());
        assertEquals("operator-2", audit.events.getLast().actorId());
        assertThrows(IdentityOperationException.class, () -> service.revoke(created.id(), "operator-2"));
        assertThrows(IdentityOperationException.class, () -> service.create("adult@example.com", " "));
    }

    private static InvitationAdminService service(InvitationRepository repository, AuditLog audit) {
        return new InvitationAdminService(repository, audit, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static final class AuditLog implements IdentityAuditLog {
        private final List<AuditEvent> events = new ArrayList<>();

        @Override
        public void record(String eventType, String actorId, String subjectId, Instant occurredAt) {
            events.add(new AuditEvent(eventType, actorId, subjectId, occurredAt));
        }
    }

    private record AuditEvent(String eventType, String actorId, String subjectId, Instant occurredAt) {
    }

    private static final class InvitationRepository implements IdentityRepository {
        private Invitation invitation;

        @Override
        public Optional<Invitation> findInvitationByCodeHash(String codeHash) {
            return invitation != null && invitation.codeHash().equals(codeHash) ? Optional.of(invitation) : Optional.empty();
        }

        @Override
        public Optional<Account> findAccountByIdentity(ExternalIdentity identity) {
            return Optional.empty();
        }

        @Override
        public Optional<Account> findAccountByEmail(EmailAddress email) {
            return Optional.empty();
        }

        @Override
        public Optional<Account> findAccountById(UUID accountId) {
            return Optional.empty();
        }

        @Override
        public Optional<ResponsibleProfile> findResponsibleProfileByAccountId(UUID accountId) {
            return Optional.empty();
        }

        @Override
        public Invitation createInvitation(Invitation invitation) {
            this.invitation = invitation;
            return invitation;
        }

        @Override
        public boolean revokeInvitation(UUID invitationId, Instant now) {
            if (invitation == null || !invitation.id().equals(invitationId)
                    || invitation.statusAt(now) != Invitation.Status.PENDING) {
                return false;
            }
            invitation = invitation.revoke(now);
            return true;
        }

        @Override
        public boolean transitionAccount(UUID accountId, Account.Status expected, Account.Status target,
                String actorId, Instant now) {
            return false;
        }

        @Override
        public RegistrationResult finalizeRegistration(UUID invitationId, String expectedCodeHash,
                Account account, ResponsibleProfile profile, Instant now) {
            throw new UnsupportedOperationException();
        }
    }
}