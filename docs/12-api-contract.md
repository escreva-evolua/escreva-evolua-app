# 12 — API Contract

Base:

```text
/api/v1
```

## Identity

```http
POST /auth/register
POST /auth/login
POST /auth/refresh
```

## Profiles

```http
GET /profiles/me
PATCH /profiles/me
GET /responsible/profiles
POST /responsible/profiles
PATCH /responsible/profiles/{profileId}
```

## Challenges

```http
GET /challenges/today
POST /challenges/{id}/start
GET /challenges/history
```

## Writing

```http
POST /writings
GET /writings/{id}
POST /writings/{id}/versions
POST /writings/{id}/images
POST /writings/{id}/transcription/confirm
POST /writings/{id}/submit
```

## Evaluation

```http
GET /writings/{id}/evaluation
POST /writings/{id}/reevaluate
POST /writings/{id}/versions/{versionId}/evaluate
```

## Progress

```http
GET /progress
GET /progress/skills
GET /progress/history
```

## Administration

```http
GET /admin/rubrics
POST /admin/rubrics
POST /admin/rubrics/{id}/versions
PATCH /admin/rubrics/{id}/versions/{versionId}
```

## API principles

- REST;
- JSON;
- idempotency para operações sensíveis;
- pagination;
- problem details para erros;
- correlation/request ID;
- versionamento;
- autorização por recurso.

## Assíncrono

Quando uma operação for assíncrona:

```json
{
  "status": "PROCESSING",
  "operationId": "..."
}
```

O cliente consulta o recurso ou recebe atualização posterior.

## Não expor

Nunca retornar:
- API keys;
- prompts internos;
- credenciais;
- dados de outro perfil;
- detalhes de safety internos desnecessários.
