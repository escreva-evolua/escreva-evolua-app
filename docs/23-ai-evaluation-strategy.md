# 23 — AI Evaluation Strategy

**Status:** proposta para especificação e experimentação.

Complementa:
- `docs/10-ai-architecture.md`
- `docs/13-evaluation-rubric.md`
- `docs/15-security-lgpd.md`
- `docs/17-metrics.md`

## Objetivo

Avaliar qualidade, segurança, consistência e custo das tarefas de IA sem depender de impressões isoladas ou executar benchmarks caros em toda alteração.

## Unidades de avaliação

Cada caso deve declarar:
- identificador estável;
- tarefa de IA;
- objetivo;
- entradas sintéticas;
- perfil etário/dificuldade quando aplicável;
- critérios esperados;
- severidade da falha;
- execução barata ou avaliação completa;
- versão da rubrica/prompt aplicável.

## Camadas de execução

### Smoke suite

Poucos casos determinísticos, executados quando um prompt, schema, policy ou adapter muda.

### Regression suite

Casos representativos para comparar versão atual e candidata de prompt/modelo.

### Safety suite

Casos de conteúdo inadequado, dados pessoais, prompt injection e limites de comportamento. Executar em toda alteração de políticas ou tarefas de IA relevantes.

### Human review

Amostras avaliadas por pessoas para critérios subjetivos, especialmente criatividade, adequação etária e utilidade do feedback.

## Métricas

- aderência ao schema;
- validade e limites das notas;
- consistência com rubrica;
- especificidade e utilidade do feedback;
- adequação etária;
- falsos positivos/negativos de safety;
- diversidade/não repetição dos desafios;
- latência;
- tokens/custo por tarefa;
- taxa de retry e fallback.

## Comparação de versões

Alterar um fator por vez quando possível: prompt, modelo, parâmetros ou rubrica.

Comparar casos idênticos e registrar:
- prompt version;
- provider/model;
- parâmetros;
- rubrica version;
- data e ambiente;
- custo/tokens quando disponíveis;
- resultados automáticos;
- avaliação humana;
- decisão e justificativa.

## Regra de promoção

Uma versão não deve ser promovida apenas por nota média superior. Bloqueadores de segurança, schema inválido, regressões severas ou violações da rubrica devem ser avaliados separadamente.

## Controle de custos

- Usar casos pequenos e sintéticos no smoke suite.
- Reservar modelos caros e avaliações humanas para casos de maior risco ou amostras.
- Não reavaliar dados de usuários reais sem base legal, política aprovada e controles de privacidade.
- Cachear resultados apenas quando entradas, versão de prompt, modelo, parâmetros e rubrica forem idênticos e a política permitir.
- Estabelecer orçamento e limite de chamadas antes de executar uma suíte extensa.

## Limitações

LLM-as-judge pode reproduzir vieses e não é uma verdade objetiva. Usar rubricas explícitas, calibração humana e análise de desacordos. Não usar avaliação automática como único critério para alegações pedagógicas ou de segurança.
