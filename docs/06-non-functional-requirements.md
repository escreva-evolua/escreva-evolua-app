# 06 — Non-Functional Requirements

## NFR-001 — Portability

O domínio não pode depender do banco de dados, LLM, mensageria ou framework de UI.

## NFR-002 — Availability

Operações de leitura do perfil e histórico devem continuar disponíveis mesmo quando o processamento de IA estiver temporariamente indisponível.

## NFR-003 — Async AI

Avaliações, geração e ingestão de imagens devem poder ser processadas assincronamente.

## NFR-004 — Reliability

Mensagens críticas devem utilizar entrega durável e mecanismos de retry/dead-letter.

## NFR-005 — Security

Segredos de LLM, banco, mensageria e storage nunca podem chegar ao cliente.

## NFR-006 — Observability

Logs estruturados, métricas e tracing devem permitir acompanhar:
- request;
- challenge generation;
- evaluation;
- image ingestion;
- LLM provider;
- retries;
- failures.

## NFR-007 — Auditability

Mudanças administrativas de rubrica, segurança, prompts e configurações devem ser auditáveis.

## NFR-008 — Explainability

A avaliação deve guardar critérios, pesos, versão da rubrica e evidências/justificativas estruturadas.

## NFR-009 — Scalability

O backend deve permitir escalar workers de IA independentemente da API.

## NFR-010 — Maintainability

Módulos devem ter fronteiras claras e testes próprios.

## NFR-011 — Accessibility

Web e mobile devem considerar acessibilidade desde o MVP.

## NFR-012 — Privacy

Privacidade por design será aprofundada em etapa específica. Dados de menores exigem tratamento restritivo.

## NFR-013 — Cost Control

Chamadas LLM devem ter limites, orçamento, métricas e políticas de retry para evitar custo descontrolado.

## NFR-014 — Model Independence

A aplicação deve conseguir trocar o provedor LLM sem alterar casos de uso.

## NFR-015 — Database Independence

A aplicação deve conseguir adicionar uma implementação relacional sem reescrever domínio e casos de uso.
