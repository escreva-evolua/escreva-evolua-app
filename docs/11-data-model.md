# 11 — Data Model

## MongoDB inicial

MongoDB é adequado ao MVP porque:
- perfis e preferências têm estrutura evolutiva;
- desafios têm metadados variados;
- avaliações podem evoluir;
- documentos JSON são naturais para resultados de IA;
- permite iniciar sem grande rigidez de esquema.

Mas o sistema não deve depender semanticamente de MongoDB.

## Coleções

```text
accounts
responsible_profiles
writer_profiles
age_profiles
challenges
challenge_deliveries
writing_submissions
writing_versions
image_submissions
transcriptions
evaluations
rubrics
rubric_versions
skill_progress
achievements
streaks
notifications
ai_requests
audit_events
```

## Identificadores

Usar IDs gerados pela aplicação, evitando expor detalhes internos do banco.

## Versionamento

Os seguintes artefatos precisam ser imutáveis após uso:
- Challenge version;
- Rubric version;
- AI prompt version;
- AI model identifier/version;
- Age profile version.

## Portabilidade relacional

Evitar:
- queries Mongo espalhadas pelo domínio;
- documentos Mongo como objetos de domínio;
- dependência direta de `MongoCollection` fora do adapter;
- regras de negócio em aggregation pipeline.

O adapter pode usar Mongo-specific queries.

Se houver migração para PostgreSQL:
- manter domínio;
- manter casos de uso;
- substituir adapter;
- substituir configuração;
- migrar dados.

## Embeddings

Inicialmente armazenar embeddings no ecossistema MongoDB.

O serviço de similaridade deve depender de:

```java
interface SimilarityRepository {
    List<SimilarityMatch> findSimilar(Embedding embedding, SimilarityQuery query);
}
```

Assim um vector database separado poderá ser adotado depois.
