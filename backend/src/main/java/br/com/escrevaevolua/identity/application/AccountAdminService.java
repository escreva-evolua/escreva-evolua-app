package br.com.escrevaevolua.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


import br.com.escrevaevolua.identity.domain.Account;

public class AccountAdminService {

    private final IdentityRepository repository;
    private final Clock clock;

    public AccountAdminService(IdentityRepository repository) {
        this(repository, Clock.systemUTC());
    }

    AccountAdminService(IdentityRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
    }

    public void transition(UUID accountId, Action action, String actorId) {
        if (actorId == null || actorId.isBlank()) {
            throw IdentityOperationException.rejected();
        }
        Account account = repository.findAccountById(Objects.requireNonNull(accountId))
                .orElseThrow(IdentityOperationException::rejected);
        Instant now = clock.instant();
        Account transitioned;
        try {
            transitioned = switch (action) {
                case BLOCK -> account.block(now);
                case UNBLOCK -> account.unblock(now);
                case DISABLE -> account.disable(now);
            };
        } catch (IllegalStateException exception) {
            throw IdentityOperationException.rejected();
        }
        if (!repository.transitionAccount(accountId, account.status(), transitioned.status(), actorId.strip(), now)) {
            throw IdentityOperationException.rejected();
        }
    }

    public enum Action {
        BLOCK,
        UNBLOCK,
        DISABLE
    }
}