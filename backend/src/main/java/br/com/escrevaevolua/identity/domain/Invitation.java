package br.com.escrevaevolua.identity.domain;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import java.time.Instant;

public record Invitation(
        UUID id,
        EmailAddress email,
        String codeHash,
        Status status,
        Instant expiresAt,
        Instant redeemedAt,
        Instant createdAt,
        Instant updatedAt) {

    public enum Status {
        PENDING,
        REDEEMED,
        EXPIRED,
        REVOKED
    }

    public Invitation {
        Objects.requireNonNull(id, "Identificador obrigatório");
        Objects.requireNonNull(email, "E-mail obrigatório");
        Objects.requireNonNull(codeHash, "Hash do convite obrigatório");
        Objects.requireNonNull(status, "Estado obrigatório");
        Objects.requireNonNull(expiresAt, "Expiração obrigatória");
        Objects.requireNonNull(createdAt, "Criação obrigatória");
        Objects.requireNonNull(updatedAt, "Atualização obrigatória");
        if (!codeHash.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("Hash do convite deve ser SHA-256 hexadecimal");
        }
        if ((status == Status.REDEEMED) != (redeemedAt != null)) {
            throw new IllegalArgumentException("Data de consumo incompatível com o estado do convite");
        }
    }

    public Status statusAt(Instant now) {
        Objects.requireNonNull(now, "Horário obrigatório");
        return status == Status.PENDING && !now.isBefore(expiresAt) ? Status.EXPIRED : status;
    }

    public boolean matches(EmailAddress candidateEmail, String candidateCodeHash, Instant now) {
        if (candidateEmail == null || candidateCodeHash == null || statusAt(now) != Status.PENDING
                || !email.equals(candidateEmail)) {
            return false;
        }
        try {
            return MessageDigest.isEqual(HexFormat.of().parseHex(codeHash), HexFormat.of().parseHex(candidateCodeHash));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public Invitation redeem(Instant now) {
        if (statusAt(now) != Status.PENDING) {
            throw new IllegalStateException("Convite indisponível");
        }
        return new Invitation(id, email, codeHash, Status.REDEEMED, expiresAt, now, createdAt, now);
    }

    public Invitation revoke(Instant now) {
        if (statusAt(now) != Status.PENDING) {
            throw new IllegalStateException("Convite indisponível");
        }
        return new Invitation(id, email, codeHash, Status.REVOKED, expiresAt, null, createdAt, now);
    }
}