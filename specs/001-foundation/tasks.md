# SPEC-001 — Tasks

- **Status:** Aprovadas para execução.
- **Spec:** `specs/001-foundation/spec.md`
- **Plan:** `specs/001-foundation/plan.md`

## Gate 0 — Decisões e aprovação

- [x] T0.1 — Aprovar o escopo da SPEC-001, o plano e esta lista de tarefas.
- [x] T0.2 — Fixar Java 25, Quarkus 3.40 LTS, Maven Wrapper 3.3.4 e distribuição Apache Maven 3.9.16 como decisões desta entrega.

## Build e estrutura

- [x] T1 — Criar `backend/` com projeto Quarkus/Maven na versão aprovada.
- [x] T2 — Versionar Maven Wrapper e fixar a versão de Java/Quarkus no build.
- [x] T2.1 — Fixar Maven Wrapper 3.3.4 e Apache Maven 3.9.16 em `.mvn/wrapper/maven-wrapper.properties`.
- [x] T3 — Configurar a aplicação para iniciar sem serviços externos e sem segredos obrigatórios.
- [x] T4 — Adicionar a extensão de health checks apropriada à versão escolhida.

## Testes e CI

- [x] T5 — Adicionar testes de integração para liveness e readiness.
- [x] T6 — Confirmar que a suíte de testes não tenta conectar a MongoDB, Redis, RabbitMQ ou LLM.
- [x] T7 — Criar `.github/workflows/backend-ci.yml` para executar build e testes somente em push para `main` e pull requests direcionados a `main`.
- [x] T8 — Confirmar que falhas de build/teste tornam a execução da CI malsucedida.

## Documentação e revisão

- [x] T9 — Documentar comandos de desenvolvimento, versão do Java/Quarkus e health endpoints.
- [x] T9.1 — Conferir alinhamento de versões entre ADR-001, README, spec e configuração real; confirmar o namespace `br.com.escrevaevolua` em fontes e testes.
- [x] T10 — Atualizar `specs/README.md` com SPEC-001 e seu status.
- [x] T11 — Revisar a arquitetura contra ADR-002/003; remover abstrações ou módulos vazios que não sejam necessários.
- [x] T12 — Executar localmente o mesmo comando de build/testes da CI e registrar comando/resultado; repetir a validação a cada feature/correção.
- [x] T12.1 — Confirmar e registrar que o CI aplicável da implementação terminou sem falhas antes de concluir a spec.
- [x] T13 — Executar quality gates de build, testes, documentação, arquitetura, compatibilidade e release notes.
- [x] T14 — Executar `git diff --check` e revisar `git diff`.
- [x] T15 — Registrar eventuais warnings, evidências e limitações; concluir somente sem bloqueadores abertos.

## Evidências e gates

- Build/testes locais: `MAVEN_USER_HOME=/tmp/escreva-evolua-maven-home ./mvnw --batch-mode --no-transfer-progress -Dmaven.repo.local=/tmp/escreva-evolua-m2 verify` — `PASS`, executado em 24/10/2026, `BUILD SUCCESS`, 2 testes de integração aprovados, 0 falhas, Java 25 e Quarkus 3.40.1.
- Execução local: `MAVEN_USER_HOME=/tmp/escreva-evolua-maven-home ./mvnw -Dmaven.repo.local=/tmp/escreva-evolua-m2 quarkus:dev` — `PASS`; liveness e readiness consultados via HTTP retornaram `UP` (20/10/2026).
- CI remoto: `PASS`, check `Build and integration tests` da PR [#2](https://github.com/escreva-evolua/escreva-evolua-app/pull/2), execução [39526644643](https://github.com/escreva-evolua/escreva-evolua-app/actions/runs/39526644643), concluída com sucesso em 21/10/2026 antes do merge. Não houve reviews nem comentários de revisão ou conversa na PR.
- Diff final (24/10/2026): `git diff --check` — `PASS`; revisão do diff sem artefatos gerados de build ou IDE.
- Gates: build `PASS`; testes `PASS`; documentação `PASS`; arquitetura `PASS`; compatibilidade `PASS` (nenhum contrato anterior de backend existia); release notes `PASS` (sem impacto de release separado nesta fundação).
- Documentação revisada: `README.md`, `backend/README.md`, `adrs/ADR-001-java-quarkus.md`, `specs/README.md`; comandos, versões, estrutura e links internos conferidos.
