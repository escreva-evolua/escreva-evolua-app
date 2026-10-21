# SPEC-001 — Implementation Plan

- **Status:** Aprovado para execução.
- **Spec:** `specs/001-foundation/spec.md`

## Estratégia

Entregar uma base backend mínima, verificável e sem integrações de negócio. As decisões de Java 25 e Quarkus 3.40 LTS já estão registradas no ADR-001 e alinhadas ao README. Gerar a estrutura do Quarkus, configurar build/testes/health checks e conectar o build à CI.

## Etapas

1. Usar Java 25 e o stream Quarkus 3.40 LTS já registrados no ADR-001 e no README; fixar a versão de manutenção compatível no build e documentá-la.
2. Criar o backend em `backend/` com ferramenta oficial Quarkus/Maven, fixando Maven Wrapper 3.3.4 (última versão estável verificada; o Apache não publica canal LTS do Wrapper) e Apache Maven 3.9.16 como distribuição executada.
3. Adicionar a extensão SmallRye Health e configurar a aplicação para iniciar sem dependências externas.
4. Implementar testes de integração HTTP para liveness/readiness e manter a suíte sem dependência de serviços remotos. Criar testes unitários somente para comportamento isolado real da aplicação, se houver.
5. Adicionar workflow GitHub Actions para build e testes com Java 25 apenas em push para `main` e pull requests direcionados a `main`.
6. Documentar pré-requisitos, comandos locais que reproduzem as etapas da CI, versão fixada e endpoints de saúde. Executar esse comando a cada feature/correção e registrar evidência de CI sem falhas em cada spec/implementação.
7. Revisar estrutura contra ADR-002 e ADR-003; evitar módulos, portas e adapters ainda sem necessidade.
8. Executar `./mvnw verify` (ou comando equivalente documentado no Windows), validar CI e registrar evidências.
9. Executar quality gates, `git diff --check` e revisão do diff.

## Artefatos esperados

- `backend/pom.xml` e Maven Wrapper.
- Código mínimo necessário para inicialização e health checks.
- Testes de health checks.
- `.github/workflows/backend-ci.yml`.
- Instruções de desenvolvimento atualizadas.
- ADR-001 e README alinhados à versão de framework escolhida.

## Limites

Não adicionar endpoints de negócio, autenticação, persistência, mensageria, integração com IA, clients web/mobile ou infraestrutura de deploy. A implementação deve se manter dentro da SPEC-001.
