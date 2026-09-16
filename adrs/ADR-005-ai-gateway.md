# ADR-005 — AI Gateway

## Status
Accepted

## Context
DeepSeek será o LLM inicial, mas o produto não deve ficar preso ao fornecedor.

## Decision
Criar AI Gateway com interfaces de tarefas e adapters de provider.

Inicial:
1. DeepSeek
2. OpenAI/Codex fallback

## Rules
- casos de uso não conhecem provider;
- provider é selecionado por policy;
- requests e outputs são normalizados;
- falhas têm retry/fallback;
- modelo, provider, prompt e rubric version são auditáveis.

## Caveat
Codex é otimizado para tarefas agentic/coding; a equivalência para avaliação de escrita deve ser validada por benchmark antes de produção.
