package br.com.escrevaevolua.identity.domain;

import java.util.Locale;

public record EmailAddress(String value) {

    public EmailAddress {
        if (value == null) {
            throw new IllegalArgumentException("E-mail obrigatório");
        }

        value = value.strip().toLowerCase(Locale.ROOT);
        int separator = value.indexOf('@');
        if (separator <= 0 || separator != value.lastIndexOf('@') || separator == value.length() - 1
                || value.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("E-mail inválido");
        }
    }
}