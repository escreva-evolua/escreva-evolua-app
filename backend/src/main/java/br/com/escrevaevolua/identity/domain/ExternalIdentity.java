package br.com.escrevaevolua.identity.domain;

import java.net.URI;
import java.util.Objects;

public record ExternalIdentity(String issuer, String subject) {

    public ExternalIdentity {
        Objects.requireNonNull(issuer, "Emissor obrigatório");
        Objects.requireNonNull(subject, "Sujeito obrigatório");
        URI issuerUri = URI.create(issuer);
        if (!issuerUri.isAbsolute() || subject.isBlank()) {
            throw new IllegalArgumentException("Identidade externa inválida");
        }
    }
}