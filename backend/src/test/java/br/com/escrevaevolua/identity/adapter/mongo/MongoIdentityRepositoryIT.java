package br.com.escrevaevolua.identity.adapter.mongo;

import java.time.Instant;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.mongodb.client.MongoClient;


import br.com.escrevaevolua.identity.application.IdentityOperationException;
import br.com.escrevaevolua.identity.application.IdentityRepository;
import br.com.escrevaevolua.identity.application.InvitationCode;
import br.com.escrevaevolua.identity.application.IdentityService;
import br.com.escrevaevolua.identity.domain.Account;
import br.com.escrevaevolua.identity.domain.EmailAddress;
import br.com.escrevaevolua.identity.domain.ExternalIdentity;
import br.com.escrevaevolua.identity.domain.Invitation;
import br.com.escrevaevolua.identity.domain.ResponsibleProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MongoIdentityRepositoryIT {

    @Autowired
    MongoIdentityRepository repository;

    @Autowired
    MongoClient mongoClient;

    @Value("${spring.mongodb.database}")
    String databaseName;

    private UUID invitationId;
    private String codeHash;
    private ExternalIdentity identity;

    @AfterEach
    void cleanUp() {
        if (invitationId == null) {
            return;
        }
        mongoClient.getDatabase(databaseName).getCollection("identity_invitations")
                .deleteOne(new Document("_id", invitationId.toString()));
        if (identity != null) {
            var accounts = mongoClient.getDatabase(databaseName).getCollection("identity_accounts");
            Document account = accounts.find(new Document("identityIssuer", identity.issuer())
                    .append("identitySubject", identity.subject())).first();
            if (account != null) {
                mongoClient.getDatabase(databaseName).getCollection("responsible_profiles")
                        .deleteOne(new Document("accountId", account.getString("_id")));
                mongoClient.getDatabase(databaseName).getCollection("identity_audit_events")
                        .deleteMany(new Document("subjectId", account.getString("_id")));
            }
            mongoClient.getDatabase(databaseName).getCollection("identity_audit_events")
                    .deleteMany(new Document("subjectId", invitationId.toString()));
            accounts.deleteOne(new Document("identityIssuer", identity.issuer())
                    .append("identitySubject", identity.subject()));
        }
    }

    @Test
    void consumesInvitationOnlyOnceUnderConcurrentFinalization() throws Exception {
        Instant now = Instant.now();
        invitationId = UUID.randomUUID();
        codeHash = InvitationCode.sha256(UUID.randomUUID().toString());
        identity = new ExternalIdentity("https://issuer.example/pool", UUID.randomUUID().toString());
        EmailAddress email = new EmailAddress("adult-" + UUID.randomUUID() + "@example.com");
        var invitations = mongoClient.getDatabase(databaseName).getCollection("identity_invitations");
        invitations.insertOne(new Document("_id", invitationId.toString())
                .append("email", email.value())
                .append("codeHash", codeHash)
                .append("status", Invitation.Status.PENDING.name())
                .append("expiresAt", Date.from(now.plusSeconds(3600)))
                .append("createdAt", Date.from(now))
                .append("updatedAt", Date.from(now)));
        assertTrue(repository.findInvitationByCodeHash(codeHash).isPresent());

        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> first = executor.submit(() -> finalizeAfter(start, email, now));
            Future<Boolean> second = executor.submit(() -> finalizeAfter(start, email, now));
            start.countDown();
            int successes = (first.get() ? 1 : 0) + (second.get() ? 1 : 0);

            assertEquals(1, successes);
        }

        assertEquals(Invitation.Status.REDEEMED,
                repository.findInvitationByCodeHash(codeHash).orElseThrow().status());
        assertEquals(Account.Status.ACTIVE, repository.findAccountByIdentity(identity).orElseThrow().status());
        assertTrue(repository.findResponsibleProfileByAccountId(
                repository.findAccountByIdentity(identity).orElseThrow().id()).isPresent());
        assertTrue(mongoClient.getDatabase(databaseName).getCollection("identity_audit_events")
                .find(new Document("subjectId", invitationId.toString()))
                .into(new ArrayList<>()).stream()
                .anyMatch(event -> "INVITATION_REDEEMED".equals(event.getString("eventType"))));
        assertUniqueIndex("identity_invitations", new Document("codeHash", 1));
        assertUniqueIndex("identity_accounts", new Document("identityIssuer", 1).append("identitySubject", 1));
        assertUniqueIndex("identity_accounts", new Document("email", 1));
        assertUniqueIndex("responsible_profiles", new Document("accountId", 1));
        assertThrows(com.mongodb.MongoWriteException.class,
                () -> mongoClient.getDatabase(databaseName).getCollection("identity_accounts")
                        .insertOne(new Document("_id", UUID.randomUUID().toString())
                                .append("identityIssuer", identity.issuer())
                                .append("identitySubject", UUID.randomUUID().toString())
                                .append("email", email.value())
                                .append("status", Account.Status.ACTIVE.name())
                                .append("createdAt", Date.from(now))
                                .append("updatedAt", Date.from(now))));
    }

    @Test
    void resumesWhenInvitationWasRedeemedBeforeAccountActivation() {
        Instant now = Instant.now();
        invitationId = UUID.randomUUID();
        String code = UUID.randomUUID().toString();
        codeHash = InvitationCode.sha256(code);
        identity = new ExternalIdentity("https://issuer.example/pool", UUID.randomUUID().toString());
        EmailAddress email = new EmailAddress("adult-" + UUID.randomUUID() + "@example.com");
        UUID accountId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        var database = mongoClient.getDatabase(databaseName);

        database.getCollection("identity_invitations").insertOne(new Document("_id", invitationId.toString())
                .append("email", email.value())
                .append("codeHash", codeHash)
                .append("status", Invitation.Status.REDEEMED.name())
                .append("expiresAt", Date.from(now.plusSeconds(3600)))
                .append("redeemedAt", Date.from(now))
                .append("redeemedIdentityIssuer", identity.issuer())
                .append("redeemedIdentitySubject", identity.subject())
                .append("createdAt", Date.from(now))
                .append("updatedAt", Date.from(now)));
        database.getCollection("identity_accounts").insertOne(new Document("_id", accountId.toString())
                .append("identityIssuer", identity.issuer())
                .append("identitySubject", identity.subject())
                .append("email", email.value())
                .append("status", Account.Status.PENDING_ACTIVATION.name())
                .append("registrationInvitationId", invitationId.toString())
                .append("createdAt", Date.from(now))
                .append("updatedAt", Date.from(now)));
        database.getCollection("responsible_profiles").insertOne(new Document("_id", profileId.toString())
                .append("accountId", accountId.toString())
                .append("displayName", "Responsável")
                .append("createdAt", Date.from(now))
                .append("updatedAt", Date.from(now)));

        IdentityService service = new IdentityService(repository, Clock.fixed(now, ZoneOffset.UTC));
        IdentityRepository.RegistrationResult result = service.finalizeRegistration(
                identity, email.value(), true, "Responsável", code);

        assertEquals(Account.Status.ACTIVE, result.account().status());
        assertEquals(profileId, result.profile().id());
        assertEquals(Invitation.Status.REDEEMED, repository.findInvitationByCodeHash(codeHash).orElseThrow().status());
        assertTrue(mongoClient.getDatabase(databaseName).getCollection("identity_audit_events")
                .find(new Document("subjectId", accountId.toString()))
                .into(new ArrayList<>()).stream()
                .anyMatch(event -> "ACCOUNT_ACTIVATED".equals(event.getString("eventType"))));
    }

    @Test
    void persistsAdministrativeAccountTransitionAudit() {
        Instant now = Instant.now();
        invitationId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        identity = new ExternalIdentity("https://issuer.example/pool", UUID.randomUUID().toString());
        mongoClient.getDatabase(databaseName).getCollection("identity_accounts")
                .insertOne(new Document("_id", accountId.toString())
                        .append("identityIssuer", identity.issuer())
                        .append("identitySubject", identity.subject())
                        .append("email", "audit-" + UUID.randomUUID() + "@example.com")
                        .append("status", Account.Status.ACTIVE.name())
                        .append("createdAt", Date.from(now))
                        .append("updatedAt", Date.from(now)));

        assertTrue(repository.transitionAccount(accountId, Account.Status.ACTIVE, Account.Status.BLOCKED,
                "operator-1", now));

        Document audit = mongoClient.getDatabase(databaseName).getCollection("identity_audit_events")
                .find(new Document("_id", "ACCOUNT_BLOCKED:" + accountId)).first();
        assertTrue(audit != null);
        assertEquals("ACCOUNT_BLOCKED", audit.getString("eventType"));
        assertEquals("operator-1", audit.getString("actorId"));
        assertEquals(accountId.toString(), audit.getString("subjectId"));
    }

    @Test
    void cannotFinalizeExpiredRevokedOrRedeemedInvitation() {
        Instant now = Instant.now();
        for (Invitation.Status status : List.of(Invitation.Status.PENDING, Invitation.Status.REVOKED,
                Invitation.Status.REDEEMED)) {
            invitationId = UUID.randomUUID();
            String code = UUID.randomUUID().toString();
            codeHash = InvitationCode.sha256(code);
            identity = new ExternalIdentity("https://issuer.example/pool", UUID.randomUUID().toString());
            EmailAddress email = new EmailAddress("adult-" + UUID.randomUUID() + "@example.com");
            Date expiration = Date.from(status == Invitation.Status.PENDING
                    ? now.minusSeconds(1)
                    : now.plusSeconds(3600));
            Document invitation = new Document("_id", invitationId.toString())
                    .append("email", email.value())
                    .append("codeHash", codeHash)
                    .append("status", status.name())
                    .append("expiresAt", expiration)
                    .append("createdAt", Date.from(now))
                    .append("updatedAt", Date.from(now));
            if (status == Invitation.Status.REDEEMED) {
                invitation.append("redeemedAt", Date.from(now));
            }
            mongoClient.getDatabase(databaseName).getCollection("identity_invitations").insertOne(invitation);
            UUID accountId = UUID.randomUUID();
            Account account = new Account(accountId, identity, email, Account.Status.PENDING_ACTIVATION, now, now);
            ResponsibleProfile profile = new ResponsibleProfile(UUID.randomUUID(), accountId, "Responsável", now, now);

            assertThrows(IdentityOperationException.class,
                    () -> repository.finalizeRegistration(invitationId, codeHash, account, profile, now));

            mongoClient.getDatabase(databaseName).getCollection("identity_accounts")
                    .deleteOne(new Document("_id", accountId.toString()));
            mongoClient.getDatabase(databaseName).getCollection("identity_invitations")
                    .deleteOne(new Document("_id", invitationId.toString()));
        }
    }

    private void assertUniqueIndex(String collectionName, Document expectedKeys) {
        List<Document> indexes = mongoClient.getDatabase(databaseName)
                .getCollection(collectionName).listIndexes().into(new ArrayList<>());
        assertTrue(indexes.stream().anyMatch(index -> Boolean.TRUE.equals(index.getBoolean("unique"))
                && expectedKeys.equals(index.get("key", Document.class))));
    }

    private boolean finalizeAfter(CountDownLatch start, EmailAddress email, Instant now) throws Exception {
        start.await();
        UUID accountId = UUID.randomUUID();
        Account account = new Account(accountId, identity, email, Account.Status.PENDING_ACTIVATION, now, now);
        ResponsibleProfile profile = new ResponsibleProfile(UUID.randomUUID(), accountId, "Responsável", now, now);
        try {
            IdentityRepository.RegistrationResult result = repository.finalizeRegistration(
                    invitationId, codeHash, account, profile, now);
            return result.account().isActive();
        } catch (IdentityOperationException exception) {
            return false;
        }
    }
}
