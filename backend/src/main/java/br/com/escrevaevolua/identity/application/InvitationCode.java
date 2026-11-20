package br.com.escrevaevolua.identity.application;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public final class InvitationCode {

    private static final SecureRandom RANDOM = new SecureRandom();

    private InvitationCode() {
    }

    public static Generated generate() {
        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return new Generated(code, sha256(code));
    }

    public static String sha256(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Código obrigatório");
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponível", exception);
        }
    }

    public record Generated(String code, String sha256Hex) {
    }
}