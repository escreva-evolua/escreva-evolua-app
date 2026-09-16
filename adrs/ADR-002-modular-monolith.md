# ADR-002 — Modular Monolith

## Status
Accepted

## Context
O produto está em descoberta e os limites de escala ainda não justificam microserviços.

## Decision
Começar como Modular Monolith.

## Rules
- módulos por domínio;
- interfaces entre módulos;
- domínio sem dependência de infraestrutura;
- adapters externos;
- testes por módulo;
- evitar acesso direto a banco entre módulos.

## Extraction rule
Extrair para serviço independente somente quando houver motivo mensurável.
