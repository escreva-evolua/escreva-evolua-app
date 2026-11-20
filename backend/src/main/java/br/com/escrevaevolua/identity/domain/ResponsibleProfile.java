package br.com.escrevaevolua.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ResponsibleProfile(UUID id, UUID accountId, String displayName, Instant createdAt, Instant updatedAt) {

    public ResponsibleProfile {
        Objects.requireNonNull(id, "Identificador obrigatório");
        Objects.requireNonNull(accountId, "Conta obrigatória");
        Objects.requireNonNull(displayName, "Nome de exibição obrigatório");
        Objects.requireNonNull(createdAt, "Criação obrigatória");
        Objects.requireNonNull(updatedAt, "Atualização obrigatória");
        displayName = displayName.strip();
        if (displayName.isEmpty()) {
            throw new IllegalArgumentException("Nome de exibição obrigatório");
        }
    }
}