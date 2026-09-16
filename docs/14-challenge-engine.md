# 14 — Challenge Engine

## Objetivo

Gerar desafios criativos adequados ao escritor sem repetir desafios anteriores.

## Inputs

- idade;
- faixa etária;
- nível;
- interesses;
- gêneros;
- habilidades a desenvolver;
- histórico;
- desafios recentes;
- frequência;
- dificuldade;
- idioma;
- preferências;
- restrições.

## Pipeline

```text
Writer Profile
     ↓
Candidate Policy
     ↓
LLM generates candidates
     ↓
Quality validation
     ↓
Age/Safety validation
     ↓
Exact duplicate check
     ↓
Semantic similarity
     ↓
Concept similarity
     ↓
Diversity scoring
     ↓
Challenge selected
     ↓
Persist version
     ↓
Deliver
```

## Geração em lote

O sistema pode gerar múltiplos candidatos antes de escolher um.

Isso reduz a chance de entregar um desafio mediano.

## Diversidade

Considerar:
- gênero;
- cenário;
- personagem;
- conflito;
- perspectiva;
- tipo de desafio;
- duração;
- habilidade.

Evitar sequência excessiva do mesmo padrão.

## Tipos

- Prompt;
- Personagem;
- Mundo;
- “E se?”;
- Restrição;
- Perspectiva;
- Desafio rápido;
- Cena;
- Reescrita.

## Anti-repetição

### Nível 1
Comparação textual.

### Nível 2
Fuzzy matching.

### Nível 3
Embedding similarity.

### Nível 4
Comparação de conceitos/tags.

## Política

Se a similaridade ultrapassar o limiar configurado:
- rejeitar;
- gerar substituto.

O limiar deve ser configurável e monitorado.

## Qualidade

Cada candidato recebe scores internos:
- creativity;
- clarity;
- ageSuitability;
- originality;
- writingPotential;
- safety.

## Cache/estoque

O sistema pode manter um pool de desafios pré-validados para reduzir latência e custo.

## Challenge lifecycle

```text
GENERATED
→ VALIDATING
→ APPROVED
→ AVAILABLE
→ DELIVERED
→ COMPLETED / SKIPPED
```

Um desafio rejeitado nunca deve ser entregue.
