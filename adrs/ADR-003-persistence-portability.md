# ADR-003 — Persistence Portability

## Status
Accepted

## Context
MongoDB é adequado ao início, mas o produto pode futuramente exigir modelo relacional.

## Decision
Usar MongoDB como adapter inicial e manter portas de persistência independentes do banco.

## Rules
- entidades de domínio não são documentos Mongo;
- repositórios são interfaces;
- queries específicas ficam no adapter;
- nenhum caso de uso importa MongoDB.

## Consequence
Uma futura implementação PostgreSQL poderá substituir/adicionar o adapter sem reescrever o domínio.
