# ADR-001 — Java 25 + Quarkus

## Status
Accepted

## Context
O backend precisa ser robusto, escalável e compatível com o conhecimento existente do projeto.

## Decision
Usar Java 25 LTS com Quarkus 3.40 LTS no início do projeto.

Para a fundação inicial, fixar Quarkus 3.40.1 no build. Atualizações de manutenção devem permanecer no stream 3.40 e ser refletidas no build e na documentação.

Java 25 é LTS desde setembro de 2025. Quarkus 3.31 adicionou suporte completo a Java 25 e Quarkus 3.40 é o stream LTS recomendado no momento.

## Consequences

Positive:
- LTS;
- stack moderna;
- bom suporte a cloud-native;
- familiaridade;
- bom caminho para containers/OpenShift.

Negative:
- dependências precisam ser validadas;
- versões devem ser fixadas e atualizadas por manutenção.
