package br.com.escrevaevolua.identity.adapter.mongo;

import java.time.Instant;
import java.util.Date;

import org.bson.Document;

import com.mongodb.client.MongoClient;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import br.com.escrevaevolua.identity.application.IdentityAuditLog;

@Component

public class MongoIdentityAuditLog implements IdentityAuditLog {

    @Autowired
    MongoClient mongoClient;

    @Value("${spring.mongodb.database:}")
    String databaseName;

    @Override
    public void record(String eventType, String actorId, String subjectId, Instant occurredAt) {
        String eventId = eventType + ":" + subjectId;
        mongoClient.getDatabase(databaseName).getCollection("identity_audit_events")
                .updateOne(new Document("_id", eventId),
                        new Document("$setOnInsert", new Document("eventType", eventType)
                                .append("actorId", actorId)
                                .append("subjectId", subjectId)
                                .append("occurredAt", Date.from(occurredAt))),
                        new com.mongodb.client.model.UpdateOptions().upsert(true));
    }
}