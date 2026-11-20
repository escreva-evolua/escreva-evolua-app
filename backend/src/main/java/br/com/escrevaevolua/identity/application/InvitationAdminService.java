package br.com.escrevaevolua.identity.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


import br.com.escrevaevolua.identity.domain.EmailAddress;
import br.com.escrevaevolua.identity.domain.Invitation;

public class InvitationAdminService {

    private static final Duration INVITATION_VALIDITY = Duration.ofDays(30);

    private final IdentityRepository repository;
    private final IdentityAuditLog auditLog;
    private final Clock clock;

    public InvitationAdminService(IdentityRepository repository, IdentityAuditLog auditLog) {
        this(repository, auditLog, Clock.systemUTC());
    }

    InvitationAdminService(IdentityRepository repository, IdentityAuditLog auditLog, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.auditLog = Objects.requireNonNull(auditLog);
        this.clock = Objects.requireNonNull(clock);
    }

    public CreatedInvitation create(String emailValue, String actorId) {
        EmailAddress email = new EmailAddress(emailValue);
        String actor = requireActor(actorId);
        InvitationCode.Generated generated = InvitationCode.generate();
        Instant now = clock.instant();
        Invitation invitation = new Invitation(UUID.randomUUID(), email, generated.sha256Hex(),
                Invitation.Status.PENDING, now.plus(INVITATION_VALIDITY), null, now, now);
        repository.createInvitation(invitation);
        auditLog.record("INVITATION_CREATED", actor, invitation.id().toString(), now);
        return new CreatedInvitation(invitation.id(), generated.code(), invitation.expiresAt());
    }

    public void revoke(UUID invitationId, String actorId) {
        Objects.requireNonNull(invitationId);
        String actor = requireActor(actorId);
        Instant now = clock.instant();
        if (!repository.revokeInvitation(invitationId, now)) {
            throw IdentityOperationException.rejected();
        }
        auditLog.record("INVITATION_REVOKED", actor, invitationId.toString(), now);
    }

    private static String requireActor(String actorId) {
        if (actorId == null || actorId.isBlank()) {
            throw IdentityOperationException.rejected();
        }
        return actorId.strip();
    }

    public record CreatedInvitation(UUID id, String code, Instant expiresAt) {
    }
}