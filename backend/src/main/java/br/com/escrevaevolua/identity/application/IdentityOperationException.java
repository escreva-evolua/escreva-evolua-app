package br.com.escrevaevolua.identity.application;

public final class IdentityOperationException extends RuntimeException {

    private static final String PUBLIC_MESSAGE = "Não foi possível concluir a operação de identidade.";
    private final boolean internalFailure;

    private IdentityOperationException(boolean internalFailure) {
        super(PUBLIC_MESSAGE);
        this.internalFailure = internalFailure;
    }

    public static IdentityOperationException rejected() {
        return new IdentityOperationException(false);
    }

    public static IdentityOperationException internalFailure() {
        return new IdentityOperationException(true);
    }

    public boolean isInternalFailure() {
        return internalFailure;
    }
}