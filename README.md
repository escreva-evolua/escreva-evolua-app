# Escreva. Receba um desafio. Evolua.

> **Nome do produto:** provisório  
> **Tagline:** Escreva. Receba um desafio. Evolua.

Plataforma de desenvolvimento de escrita criativa apoiada por IA. O produto gera desafios personalizados por idade, nível, interesses e histórico; recebe textos por editor ou fotografia; avalia a produção com uma rubrica configurável; fornece feedback; e acompanha a evolução do escritor.

## Status

**Product Discovery — v0.1**

Este repositório contém a especificação funcional e arquitetural e inclui a fundação executável do backend. O produto ainda está em Product Discovery; consulte [backend/README.md](backend/README.md) para build, execução local e health checks.

## Objetivos do MVP

1. Atender prioritariamente usuários de 7–17 anos.
2. Estruturar desde o início suporte ao público adulto.
3. Permitir contas de responsáveis maiores de 18 anos para perfis menores de 16.
4. Gerar desafios criativos adequados à idade.
5. Garantir não repetição semântica de desafios.
6. Permitir escrita por editor e envio de fotos de textos manuscritos.
7. Avaliar textos com nota de 0 a 100.
8. Avaliar criatividade, desenvolvimento, coesão, coerência, vocabulário, estrutura, gramática, pontuação e aderência ao desafio.
9. Manter rubrica e pesos configuráveis.
10. Registrar evolução por habilidade e por aniversário.
11. Usar DeepSeek inicialmente e manter o AI Gateway independente de fornecedor.
12. Usar RabbitMQ para processamento assíncrono inicial.
13. Usar MongoDB inicialmente, preservando portabilidade para banco relacional.

## Stack inicial

- Java 25 LTS
- Quarkus 3.40 LTS (backend fixado em 3.40.1)
- MongoDB
- RabbitMQ
- Redis
- Angular
- Flutter
- Docker
- Kubernetes/OpenShift
- DeepSeek como LLM primário
- OpenAI/Codex como fallback inicialmente previsto
- Embeddings inicialmente no ecossistema MongoDB

## Princípio arquitetural central

O domínio e os casos de uso não podem depender de MongoDB, RabbitMQ, DeepSeek, Codex, Angular ou Flutter. Dependências externas devem entrar por portas/interfaces e adaptadores.

## Estrutura

```text
/
├── README.md
├── backend/
│   ├── pom.xml
│   ├── README.md
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── .mvn/wrapper/
│   └── src/
├── .github/workflows/backend-ci.yml
├── docs/
│   ├── 01-product-vision.md
│   ├── 02-personas.md
│   ├── 03-user-journeys.md
│   ├── 04-product-principles.md
│   ├── 05-functional-requirements.md
│   ├── 06-non-functional-requirements.md
│   ├── 07-mvp.md
│   ├── 08-domain-model.md
│   ├── 09-architecture.md
│   ├── 10-ai-architecture.md
│   ├── 11-data-model.md
│   ├── 12-api-contract.md
│   ├── 13-evaluation-rubric.md
│   ├── 14-challenge-engine.md
│   ├── 15-security-lgpd.md
│   ├── 16-gamification.md
│   ├── 17-metrics.md
│   ├── 18-roadmap.md
│   └── 19-backlog.md
├── adrs/
│   ├── ADR-001-java-quarkus.md
│   ├── ADR-002-modular-monolith.md
│   ├── ADR-003-persistence-portability.md
│   ├── ADR-004-rabbitmq.md
│   ├── ADR-005-ai-gateway.md
│   └── ADR-006-mobile-flutter.md
└── specs/
    └── README.md
```

## Decisões ainda abertas

- Nome definitivo e disponibilidade de marca/domínio.
- Política detalhada de privacidade e retenção de textos.
- Política de consentimento parental e requisitos legais finais.
- Pesos definitivos da rubrica após validação.
- Provedor/modelo específico de visão/OCR para manuscritos.
- Benchmark de DeepSeek versus fallback antes do go-live.
- Estratégia comercial e preços.

## Ordem recomendada de implementação

Product Discovery → Specs → ADRs → domínio → adapters → API → IA → web/mobile → observabilidade → hardening → beta.
