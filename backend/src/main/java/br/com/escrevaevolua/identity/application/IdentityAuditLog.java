package br.com.escrevaevolua.identity.application;

import java.time.Instant;

public interface IdentityAuditLog {

    void record(String eventType, String actorId, String subjectId, Instant occurredAt);
}