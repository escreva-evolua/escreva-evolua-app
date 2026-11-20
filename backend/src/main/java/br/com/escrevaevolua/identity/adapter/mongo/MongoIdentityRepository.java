package br.com.escrevaevolua.identity.adapter.mongo;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReturnDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import br.com.escrevaevolua.identity.application.IdentityOperationException;
import br.com.escrevaevolua.identity.application.IdentityAuditLog;
import br.com.escrevaevolua.identity.application.IdentityRepository;
import br.com.escrevaevolua.identity.domain.Account;
import br.com.escrevaevolua.identity.domain.EmailAddress;
import br.com.escrevaevolua.identity.domain.ExternalIdentity;
import br.com.escrevaevolua.identity.domain.Invitation;
import br.com.escrevaevolua.identity.domain.ResponsibleProfile;

@Component

public class MongoIdentityRepository implements IdentityRepository {
    private static final Logger LOG = LoggerFactory.getLogger(MongoIdentityRepository.class);


    private static final String INVITATIONS = "identity_invitations";
    private static final String ACCOUNTS = "identity_accounts";
    private static final String PROFILES = "responsible_profiles";
    private static final Duration LEASE_DURATION = Duration.ofMinutes(2);

    @Autowired
    MongoClient mongoClient;

    @Value("${spring.mongodb.database:}")
    String databaseName;

    @Autowired
    IdentityAuditLog auditLog;

    private final AtomicBoolean indexesReady = new AtomicBoolean();

    @Override
    public Optional<Invitation> findInvitationByCodeHash(String codeHash) {
        ensureIndexes();
        return Optional.ofNullable(invitations().find(new Document("codeHash", codeHash)).first())
                .map(MongoIdentityRepository::toInvitation);
    }

    @Override
    public Optional<Account> findAccountByIdentity(ExternalIdentity identity) {
        ensureIndexes();
        return Optional.ofNullable(accounts().find(identityFilter(identity)).first())
                .map(MongoIdentityRepository::toAccount);
    }

    @Override
    public Optional<Account> findAccountByEmail(EmailAddress email) {
        ensureIndexes();
        return Optional.ofNullable(accounts().find(new Document("email", email.value())).first())
                .map(MongoIdentityRepository::toAccount);
    }

    @Override
    public Optional<Account> findAccountById(UUID accountId) {
        ensureIndexes();
        return Optional.ofNullable(accounts().find(new Document("_id", accountId.toString())).first())
                .map(MongoIdentityRepository::toAccount);
    }

    @Override
    public Optional<ResponsibleProfile> findResponsibleProfileByAccountId(UUID accountId) {
        ensureIndexes();
        return Optional.ofNullable(profiles().find(new Document("accountId", accountId.toString())).first())
                .map(MongoIdentityRepository::toProfile);
    }

    @Override
    public Invitation createInvitation(Invitation invitation) {
        ensureIndexes();
        try {
            invitations().insertOne(invitationDocument(invitation));
            return invitation;
        } catch (RuntimeException exception) {
            LOG.error("Invitation persistence failed ({})", exception.getClass().getSimpleName());
            throw IdentityOperationException.internalFailure();
        }
    }

    @Override
    public boolean revokeInvitation(UUID invitationId, Instant now) {
        ensureIndexes();
        Document filter = new Document("_id", invitationId.toString())
            .append("status", Invitation.Status.PENDING.name())
            .append("$or", java.util.List.of(
                new Document("leaseExpiresAt", new Document("$exists", false)),
                new Document("leaseExpiresAt", new Document("$lte", Date.from(now)))
            ));
        Document update = new Document("$set", new Document("status", Invitation.Status.REVOKED.name())
                .append("updatedAt", Date.from(now)));
        try {
            return invitations().updateOne(filter, update).getModifiedCount() == 1;
        } catch (RuntimeException exception) {
            LOG.error("Invitation revocation failed ({})", exception.getClass().getSimpleName());
            throw IdentityOperationException.internalFailure();
        }
    }

    @Override
    public boolean transitionAccount(UUID accountId, Account.Status expected, Account.Status target,
            String actorId, Instant now) {
        ensureIndexes();
        Document filter = new Document("_id", accountId.toString()).append("status", expected.name());
        Document update = new Document("$set", new Document("status", target.name())
                .append("updatedAt", Date.from(now)));
        try {
            if (accounts().updateOne(filter, update).getModifiedCount() != 1) {
                return false;
            }
            auditLog.record("ACCOUNT_" + target.name(), actorId, accountId.toString(), now);
            return true;
        } catch (RuntimeException exception) {
            LOG.error("Account transition failed ({})", exception.getClass().getSimpleName());
            throw IdentityOperationException.internalFailure();
        }
    }

    @Override
    public RegistrationResult finalizeRegistration(
            UUID invitationId,
            String expectedCodeHash,
            Account requestedAccount,
            ResponsibleProfile requestedProfile,
            Instant now) {
        ensureIndexes();
        String leaseOwner = UUID.randomUUID().toString();
        Date leaseUntil = Date.from(now.plus(LEASE_DURATION));
        Document account = acquireAccountLease(requestedAccount, invitationId, leaseOwner, leaseUntil, now);
        if (account == null) {
            throw IdentityOperationException.rejected();
        }

        try {
            if (!requestedAccount.email().value().equals(account.getString("email"))) {
                throw IdentityOperationException.rejected();
            }

            Document invitation = invitations().find(new Document("_id", invitationId.toString())
                    .append("codeHash", expectedCodeHash)).first();
            if (invitation == null) {
                throw IdentityOperationException.rejected();
            }

            if (isAlreadyRedeemedFor(invitation, requestedAccount.identity(), invitationId)
                    && invitationId.toString().equals(account.getString("registrationInvitationId"))) {
                auditLog.record("INVITATION_REDEEMED", "system", invitationId.toString(), now);
                auditLog.record("ACCOUNT_ACTIVATED", "system", account.getString("_id"), now);
                activateAccount(account.getString("_id"), leaseOwner, now);
                return loadRegistrationResult(requestedAccount.identity());
            }

            if (!reserveInvitation(invitationId, expectedCodeHash, requestedAccount.email(), leaseOwner, leaseUntil, now)) {
                throw IdentityOperationException.rejected();
            }

            ensureProfile(requestedProfile);
            Document consumeFilter = new Document("_id", invitationId.toString())
                    .append("codeHash", expectedCodeHash)
                    .append("status", Invitation.Status.PENDING.name())
                    .append("leaseOwner", leaseOwner)
                    .append("leaseExpiresAt", new Document("$gt", Date.from(now)));
            Document consumeUpdate = new Document("$set", new Document("status", Invitation.Status.REDEEMED.name())
                    .append("redeemedAt", Date.from(now))
                    .append("updatedAt", Date.from(now))
                    .append("redeemedIdentityIssuer", requestedAccount.identity().issuer())
                    .append("redeemedIdentitySubject", requestedAccount.identity().subject()))
                    .append("$unset", new Document("leaseOwner", "").append("leaseExpiresAt", ""));
            if (invitations().updateOne(consumeFilter, consumeUpdate).getModifiedCount() != 1) {
                throw IdentityOperationException.rejected();
            }

            auditLog.record("INVITATION_REDEEMED", "system", invitationId.toString(), now);
            auditLog.record("ACCOUNT_ACTIVATED", "system", account.getString("_id"), now);
            activateAccount(account.getString("_id"), leaseOwner, now);
            return loadRegistrationResult(requestedAccount.identity());
        } catch (IdentityOperationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            LOG.error("Identity registration persistence failed ({})", exception.getClass().getSimpleName());
            throw IdentityOperationException.internalFailure();
        } finally {
            releaseLease(invitations(), invitationId.toString(), leaseOwner);
            releaseLease(accounts(), account.getString("_id"), leaseOwner);
        }
    }

    private Document acquireAccountLease(Account requestedAccount, UUID invitationId, String leaseOwner,
            Date leaseUntil, Instant now) {
        ExternalIdentity identity = requestedAccount.identity();
        Document filter = identityFilter(identity)
                .append("status", Account.Status.PENDING_ACTIVATION.name())
                .append("$or", java.util.List.of(
                        new Document("registrationLeaseExpiresAt", new Document("$exists", false)),
                        new Document("registrationLeaseExpiresAt", new Document("$lte", Date.from(now)))
                ));
        Document insert = accountDocument(requestedAccount);
        insert.remove("status");
        insert.remove("updatedAt");
        Document update = new Document("$setOnInsert", insert)
                .append("$set", new Document("registrationLeaseOwner", leaseOwner)
                        .append("registrationLeaseExpiresAt", leaseUntil)
                        .append("registrationInvitationId", invitationId.toString()));
        try {
            return accounts().findOneAndUpdate(filter, update,
                    new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
        } catch (RuntimeException exception) {
            LOG.warn("Identity account lease unavailable ({})", exception.getClass().getSimpleName());
            return null;
        }
    }

    private boolean reserveInvitation(UUID invitationId, String codeHash, EmailAddress email, String leaseOwner,
            Date leaseUntil, Instant now) {
        Document filter = new Document("_id", invitationId.toString())
                .append("codeHash", codeHash)
                .append("email", email.value())
                .append("status", Invitation.Status.PENDING.name())
                .append("expiresAt", new Document("$gt", Date.from(now)))
                .append("$or", java.util.List.of(
                        new Document("leaseExpiresAt", new Document("$exists", false)),
                        new Document("leaseExpiresAt", new Document("$lte", Date.from(now)))
                ));
        Document update = new Document("$set", new Document("leaseOwner", leaseOwner)
                .append("leaseExpiresAt", leaseUntil));
        return invitations().findOneAndUpdate(filter, update,
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)) != null;
    }

    private void ensureProfile(ResponsibleProfile profile) {
        profiles().updateOne(new Document("accountId", profile.accountId().toString()),
                new Document("$setOnInsert", profileDocument(profile)),
                new com.mongodb.client.model.UpdateOptions().upsert(true));
    }

    private void activateAccount(String accountId, String leaseOwner, Instant now) {
        Document filter = new Document("_id", accountId)
                .append("registrationLeaseOwner", leaseOwner)
                .append("status", Account.Status.PENDING_ACTIVATION.name());
        Document update = new Document("$set", new Document("status", Account.Status.ACTIVE.name())
                .append("updatedAt", Date.from(now)))
                .append("$unset", new Document("registrationLeaseOwner", "")
                        .append("registrationLeaseExpiresAt", ""));
        if (accounts().updateOne(filter, update).getModifiedCount() != 1) {
            throw IdentityOperationException.rejected();
        }
    }

    private RegistrationResult loadRegistrationResult(ExternalIdentity identity) {
        Document accountDocument = accounts().find(identityFilter(identity)).first();
        if (accountDocument == null) {
            throw IdentityOperationException.rejected();
        }
        Account account = toAccount(accountDocument);
        ResponsibleProfile profile = profiles().find(new Document("accountId", account.id().toString()))
                .map(MongoIdentityRepository::toProfile)
                .first();
        if (profile == null) {
            throw IdentityOperationException.rejected();
        }
        return new RegistrationResult(account, profile);
    }

    private boolean isAlreadyRedeemedFor(Document invitation, ExternalIdentity identity, UUID invitationId) {
        return Invitation.Status.REDEEMED.name().equals(invitation.getString("status"))
                && invitationId.toString().equals(invitation.getString("_id"))
                && identity.issuer().equals(invitation.getString("redeemedIdentityIssuer"))
                && identity.subject().equals(invitation.getString("redeemedIdentitySubject"));
    }

    private void releaseLease(MongoCollection<Document> collection, String id, String leaseOwner) {
        try {
            collection.updateOne(new Document("_id", id).append("leaseOwner", leaseOwner),
                    new Document("$unset", new Document("leaseOwner", "").append("leaseExpiresAt", "")));
            collection.updateOne(new Document("_id", id).append("registrationLeaseOwner", leaseOwner),
                    new Document("$unset", new Document("registrationLeaseOwner", "")
                            .append("registrationLeaseExpiresAt", "")));
        } catch (RuntimeException ignored) {
            LOG.warn("Identity lease cleanup deferred ({})", ignored.getClass().getSimpleName());
        }
    }

    private void ensureIndexes() {
        if (indexesReady.get()) {
            return;
        }
        invitations().createIndex(Indexes.ascending("codeHash"), new IndexOptions().unique(true));
        accounts().createIndex(Indexes.ascending("identityIssuer", "identitySubject"),
                new IndexOptions().unique(true));
        accounts().createIndex(Indexes.ascending("email"), new IndexOptions().unique(true));
        profiles().createIndex(Indexes.ascending("accountId"), new IndexOptions().unique(true));
        indexesReady.set(true);
    }

    private MongoDatabase database() {
        return mongoClient.getDatabase(databaseName);
    }

    private MongoCollection<Document> invitations() {
        return database().getCollection(INVITATIONS);
    }

    private MongoCollection<Document> accounts() {
        return database().getCollection(ACCOUNTS);
    }

    private MongoCollection<Document> profiles() {
        return database().getCollection(PROFILES);
    }

    private static Document identityFilter(ExternalIdentity identity) {
        return new Document("identityIssuer", identity.issuer()).append("identitySubject", identity.subject());
    }

    private static Document accountDocument(Account account) {
        return new Document("_id", account.id().toString())
                .append("identityIssuer", account.identity().issuer())
                .append("identitySubject", account.identity().subject())
                .append("email", account.email().value())
                .append("status", account.status().name())
                .append("createdAt", Date.from(account.createdAt()))
                .append("updatedAt", Date.from(account.updatedAt()));
    }

    private static Document invitationDocument(Invitation invitation) {
        return new Document("_id", invitation.id().toString())
                .append("email", invitation.email().value())
                .append("codeHash", invitation.codeHash())
                .append("status", invitation.status().name())
                .append("expiresAt", Date.from(invitation.expiresAt()))
                .append("createdAt", Date.from(invitation.createdAt()))
                .append("updatedAt", Date.from(invitation.updatedAt()));
    }

    private static Document profileDocument(ResponsibleProfile profile) {
        return new Document("_id", profile.id().toString())
                .append("accountId", profile.accountId().toString())
                .append("displayName", profile.displayName())
                .append("createdAt", Date.from(profile.createdAt()))
                .append("updatedAt", Date.from(profile.updatedAt()));
    }

    private static Invitation toInvitation(Document document) {
        Date redeemedAt = document.getDate("redeemedAt");
        return new Invitation(UUID.fromString(document.getString("_id")),
                new EmailAddress(document.getString("email")), document.getString("codeHash"),
                Invitation.Status.valueOf(document.getString("status")), document.getDate("expiresAt").toInstant(),
                redeemedAt == null ? null : redeemedAt.toInstant(), document.getDate("createdAt").toInstant(),
                document.getDate("updatedAt").toInstant());
    }

    private static Account toAccount(Document document) {
        return new Account(UUID.fromString(document.getString("_id")),
                new ExternalIdentity(document.getString("identityIssuer"), document.getString("identitySubject")),
                new EmailAddress(document.getString("email")), Account.Status.valueOf(document.getString("status")),
                document.getDate("createdAt").toInstant(), document.getDate("updatedAt").toInstant());
    }

    private static ResponsibleProfile toProfile(Document document) {
        return new ResponsibleProfile(UUID.fromString(document.getString("_id")),
                UUID.fromString(document.getString("accountId")), document.getString("displayName"),
                document.getDate("createdAt").toInstant(), document.getDate("updatedAt").toInstant());
    }
}