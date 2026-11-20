package br.com.escrevaevolua.identity.domain;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentityDomainTest {

    private static final Instant NOW = Instant.parse("2026-10-30T12:00:00Z");
    private static final String CODE_HASH = "a".repeat(64);

    @Test
    void normalizesEmailWithoutProviderSpecificRules() {
        assertEquals(new EmailAddress("person+tag@example.com"), new EmailAddress(" Person+Tag@Example.COM "));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("invalid email@example.com"));
    }

    @Test
    void invitationCanOnlyBeMatchedAndRedeemedWhilePendingAndUnexpired() {
        Invitation invitation = invitation();

        assertTrue(invitation.matches(new EmailAddress("adult@example.com"), CODE_HASH, NOW));
        assertFalse(invitation.matches(new EmailAddress("other@example.com"), CODE_HASH, NOW));
        assertFalse(invitation.matches(invitation.email(), "not-a-hash", NOW));
        assertEquals(Invitation.Status.EXPIRED, invitation.statusAt(NOW.plusSeconds(86400)));
        assertThrows(IllegalStateException.class, () -> invitation.redeem(NOW.plusSeconds(86400)));
        Invitation redeemed = invitation.redeem(NOW);
        assertEquals(Invitation.Status.REDEEMED, redeemed.status());
        assertThrows(IllegalStateException.class, () -> redeemed.redeem(NOW));
    }

    @Test
    void accountEnforcesOnlySpecifiedLifecycleTransitions() {
        Account account = new Account(UUID.randomUUID(), new ExternalIdentity("https://issuer.example/pool", "subject"),
                new EmailAddress("adult@example.com"), Account.Status.PENDING_ACTIVATION, NOW, NOW);

        Account active = account.activate(NOW.plusSeconds(1));
        assertTrue(active.isActive());
        assertEquals(Account.Status.BLOCKED, active.block(NOW.plusSeconds(2)).status());
        assertEquals(Account.Status.ACTIVE, active.block(NOW.plusSeconds(2)).unblock(NOW.plusSeconds(3)).status());
        assertEquals(Account.Status.DISABLED, active.disable(NOW.plusSeconds(4)).status());
        assertThrows(IllegalStateException.class, () -> account.block(NOW));
    }

    @Test
    void responsibleProfileTrimsDisplayName() {
        ResponsibleProfile profile = new ResponsibleProfile(UUID.randomUUID(), UUID.randomUUID(), " Ana ", NOW, NOW);

        assertEquals("Ana", profile.displayName());
        assertThrows(IllegalArgumentException.class,
                () -> new ResponsibleProfile(UUID.randomUUID(), UUID.randomUUID(), "  ", NOW, NOW));
    }

    private Invitation invitation() {
        return new Invitation(UUID.randomUUID(), new EmailAddress("adult@example.com"), CODE_HASH,
                Invitation.Status.PENDING, NOW.plusSeconds(86400), null, NOW, NOW);
    }
}