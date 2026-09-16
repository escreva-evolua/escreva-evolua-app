# ADR-004 — RabbitMQ para mensageria inicial

## Status
Accepted

## Context
O MVP precisa de jobs assíncronos, retry, dead-letter e distribuição de trabalho. Não há requisito inicial de event streaming massivo.

## Decision
Usar RabbitMQ inicialmente.

Workloads:
- AI evaluation;
- challenge generation;
- image ingestion;
- notifications.

RabbitMQ é adequado para task/job queues e possui mecanismos úteis de retry, TTL e dead-letter.

Kafka poderá ser introduzido somente se surgir uma necessidade real de event streaming, retenção/replay em escala ou outra capacidade específica.

## Consequence
Menor complexidade operacional inicial.
