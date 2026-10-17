# Escreva & Evolua — Additional Product Assets v0.1

Este pacote complementa a documentação existente. Ele foi preparado após inspeção do ZIP do projeto.

## Antes de copiar

O repositório já possui documentos de referência para:
- princípios do produto: `docs/04-product-principles.md`
- rubrica de avaliação: `docs/13-evaluation-rubric.md`
- segurança, menores e LGPD: `docs/15-security-lgpd.md`
- arquitetura de IA: `docs/10-ai-architecture.md`
- métricas: `docs/17-metrics.md`
- ADRs: `adrs/ADR-*.md`

Por isso, este pacote não duplica esses documentos. Ele acrescenta diretrizes operacionais, casos de avaliação de IA, planejamento de avaliação de prompts, princípios de design e estratégia de testes mobile.

## Instalação

Copie as pastas e arquivos deste pacote para a raiz do repositório, preservando os caminhos.

Arquivos incluídos:
- `docs/20-writing-feedback-guidelines.md`
- `docs/21-design-system.md`
- `docs/22-mobile-testing.md`
- `docs/23-ai-evaluation-strategy.md`
- `docs/24-child-safety-checklist.md`
- `ai/evaluation/README.md`
- `ai/evaluation/cases/*.yaml`
- `ai/prompt-evaluation/README.md`
- `ai/prompt-evaluation/record-template.yaml`

## Estado dos documentos

Estes arquivos são assets de design e avaliação para a fase de discovery/specification. Não representam funcionalidades implementadas nem substituem revisão jurídica, avaliação especializada de educação/psicologia ou aprovação de produto.

## Princípio de custo

Os casos de avaliação são pequenos e direcionados. Execute primeiro um conjunto de smoke cases e expanda a avaliação quando mudar prompts, modelos, rubricas, políticas de segurança ou roteamento de provedores. Evite executar uma suíte extensa em toda alteração de código não relacionada à IA.
