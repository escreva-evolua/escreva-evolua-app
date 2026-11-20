package br.com.escrevaevolua.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Account(
        UUID id,
        ExternalIdentity identity,
        EmailAddress email,
        Status status,
        Instant createdAt,
        Instant updatedAt) {

    public enum Status {
        PENDING_ACTIVATION,
        ACTIVE,
        BLOCKED,
        DISABLED
    }

    public Account {
        Objects.requireNonNull(id, "Identificador obrigatório");
        Objects.requireNonNull(identity, "Identidade externa obrigatória");
        Objects.requireNonNull(email, "E-mail obrigatório");
        Objects.requireNonNull(status, "Estado obrigatório");
        Objects.requireNonNull(createdAt, "Criação obrigatória");
        Objects.requireNonNull(updatedAt, "Atualização obrigatória");
    }

    public Account activate(Instant now) {
        return transition(Status.PENDING_ACTIVATION, Status.ACTIVE, now);
    }

    public Account block(Instant now) {
        return transition(Status.ACTIVE, Status.BLOCKED, now);
    }

    public Account unblock(Instant now) {
        return transition(Status.BLOCKED, Status.ACTIVE, now);
    }

    public Account disable(Instant now) {
        if (status != Status.ACTIVE && status != Status.BLOCKED) {
            throw new IllegalStateException("Transição de conta não permitida: " + status + " -> DISABLED");
        }
        return new Account(id, identity, email, Status.DISABLED, createdAt, now);
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    private Account transition(Status expected, Status target, Instant now) {
        Objects.requireNonNull(now, "Horário obrigatório");
        if (status != expected) {
            throw new IllegalStateException("Transição de conta não permitida: " + status + " -> " + target);
        }
        return new Account(id, identity, email, target, createdAt, now);
    }
}