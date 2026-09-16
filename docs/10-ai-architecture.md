# 10 — AI Architecture

## AI Gateway

O sistema deve possuir uma camada própria de abstração:

```text
Application
    ↓
AI Gateway
    ↓
AI Provider Interface
    ├── DeepSeekProvider
    ├── OpenAICodexProvider
    └── Future providers
```

## Interface conceitual

```java
interface AIProvider {
    ChallengeGenerationResult generateChallenge(ChallengeGenerationRequest request);
    EvaluationResult evaluate(EvaluationRequest request);
    FeedbackResult generateFeedback(FeedbackRequest request);
}
```

Também deve existir:

```java
interface AIProviderRouter {
    AIProvider resolve(AITask task, AIContext context);
}
```

## Provedor inicial

### Primário
DeepSeek.

A API atual do DeepSeek possui formato compatível com APIs OpenAI/Anthropic e suporta saída estruturada, o que facilita o adapter.

### Fallback
OpenAI/Codex inicialmente previsto pelo produto.

**Nota arquitetural:** modelos Codex são otimizados para tarefas agentic/coding. Portanto, o fallback deve ser tratado como capacidade técnica disponível e submetido a benchmark específico antes de ser considerado equivalente ao DeepSeek para avaliação literária.

## Estratégia

```text
request
 ↓
policy
 ↓
DeepSeek
 ↓
success?
 ├── yes → result
 └── no
       ↓
    retry policy
       ↓
    fallback
       ↓
    Codex/OpenAI
```

Falhas de conteúdo/qualidade não devem automaticamente ser tratadas como falhas de infraestrutura. Podemos precisar de uma segunda avaliação.

## AI Tasks

- `CHALLENGE_GENERATION`
- `CHALLENGE_VALIDATION`
- `CHALLENGE_SAFETY`
- `TEXT_TRANSCRIPTION`
- `TEXT_EVALUATION`
- `FEEDBACK_GENERATION`
- `PROGRESS_ANALYSIS`

## Prompt Management

Prompts devem ser versionados no repositório.

```text
ai/
  prompts/
    challenge/
    validation/
    safety/
    evaluation/
    feedback/
```

Cada execução registra:
- prompt version;
- provider;
- model;
- model version/snapshot quando disponível;
- parameters;
- rubric version;
- age profile version.

## Structured Output

Sempre que possível, o LLM deve retornar JSON validável.

Exemplo:

```json
{
  "score": 87,
  "criteria": {
    "creativity": 94,
    "development": 86,
    "cohesion": 88,
    "coherence": 90,
    "vocabulary": 82,
    "structure": 84,
    "grammar": 85,
    "punctuation": 83,
    "challengeAdherence": 91
  },
  "strengths": [],
  "improvements": []
}
```

A aplicação nunca deve confiar cegamente no JSON do modelo: validar limites, pesos e consistência.

## Safety Layer

```text
LLM generation
↓
Safety validator
↓
Age suitability validator
↓
Challenge validator
↓
Similarity validator
↓
Publish
```

Para menores:
- bloquear conteúdo sexual;
- violência inadequada à faixa etária;
- incentivo a automutilação;
- drogas;
- conteúdo extremista;
- instruções perigosas;
- outros conteúdos incompatíveis com a faixa etária.

A taxonomia final será definida em requisito de safety.

## Image ingestion

Fotos devem passar por uma abstração:

```java
interface TextExtractionProvider {
    ExtractionResult extract(ImageDocument image);
}
```

Isso permite usar:
- DeepSeek vision quando adequado;
- outro OCR;
- outro provedor multimodal.

O resultado precisa conter confiança por página/bloco quando disponível.
