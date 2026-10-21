# Specs

As specs de implementação transformam os documentos de Product Discovery em incrementos pequenos, aprováveis e verificáveis. Nenhuma spec deve ser implementada antes de seu escopo, critérios de aceitação e decisões bloqueadoras serem aprovados.

## Specs atuais

| ID | Spec | Objetivo | Status |
|---|---|---|---|
| 001 | [Backend Foundation](001-foundation/spec.md) | Criar backend Quarkus executável, build reproduzível, health checks, testes e CI | Aprovada |

Para a SPEC-001, consulte também o [plano](001-foundation/plan.md) e as [tarefas](001-foundation/tasks.md), ambos aprovados. A spec fixa o stream Quarkus 3.40 LTS, o Maven Wrapper 3.3.4 e a distribuição Apache Maven 3.9.16; a versão de manutenção Quarkus deve ser registrada no build durante a implementação.

Em cada implementação de feature ou correção, execute localmente o comando de build/testes definido pela CI e registre o resultado. Cada spec/implementação deve validar o CI aplicável sem falhas antes de ser concluída. Para o backend, a CI roda em push para `main` e em pull requests direcionados a `main`.

## Convenção de organização

```text
specs/
  README.md
  001-foundation/
    spec.md
    plan.md
    tasks.md
  002-identity/
    spec.md
    plan.md
    tasks.md
  003-writer-profile/
    spec.md
    plan.md
    tasks.md
  004-challenge-engine/
    spec.md
    plan.md
    tasks.md
```

Os próximos IDs devem ser atribuídos conforme o backlog e as dependências reais, sem criar specs antecipadas só para preencher a estrutura.

## Conteúdo obrigatório de cada spec

- objetivo e problema;
- escopo e itens explicitamente fora do escopo;
- requisitos comportamentais;
- critérios de aceitação verificáveis;
- invariantes e casos de erro;
- dependências, decisões e perguntas abertas;
- estratégia de testes;
- Definition of Done.

Cada spec aprovada deve possuir um plano de implementação e tarefas verificáveis. Decisões arquiteturais relevantes devem ser registradas em ADR antes da implementação, conforme `.darp/lifecycle.md`.
