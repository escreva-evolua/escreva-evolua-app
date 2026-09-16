# 13 — Evaluation Rubric

## Rubrica padrão inicial

| Critério | Peso |
|---|---:|
| Criatividade | 20% |
| Desenvolvimento | 20% |
| Coerência | 15% |
| Coesão | 10% |
| Vocabulário | 10% |
| Estrutura | 10% |
| Gramática | 5% |
| Pontuação | 5% |
| Aderência ao desafio | 5% |

**Observação:** os pesos são configuráveis. Não são constantes de código.

Os pesos podem ser ajustados futuramente por:
- administrador global;
- responsável, dentro do escopo autorizado do menor;
- educador, dentro do escopo da turma — futuro.

## Regra matemática

```text
score = Σ(criteriaScore × weight)
```

O sistema deve validar:
- pesos não negativos;
- soma = 100%;
- critérios ativos;
- limites 0–100.

## Coesão

A análise deve verificar a ligação entre partes do texto:
- conectivos;
- referências;
- progressão;
- repetição inadequada;
- transições;
- encadeamento de ideias.

## Coerência

Avaliar se o conjunto de ideias faz sentido dentro da história/texto.

## Aderência ao desafio

Responder explicitamente:

1. O tema foi abordado?
2. Os requisitos do desafio foram cumpridos?
3. A resposta se desviou excessivamente?
4. O desvio é intencional e criativo ou indica não atendimento?

Criatividade não deve ser penalizada simplesmente por fugir de uma interpretação óbvia.

## Adequação etária

A expectativa de qualidade deve variar conforme:
- idade;
- nível;
- tipo de desafio;
- dificuldade.

Não comparar diretamente notas de pessoas de idades muito diferentes.

## Configuração

Exemplo conceitual:

```json
{
  "name": "Default Creative Writing",
  "version": 1,
  "criteria": [
    {"key": "creativity", "weight": 20},
    {"key": "development", "weight": 20},
    {"key": "coherence", "weight": 15},
    {"key": "cohesion", "weight": 10},
    {"key": "vocabulary", "weight": 10},
    {"key": "structure", "weight": 10},
    {"key": "grammar", "weight": 5},
    {"key": "punctuation", "weight": 5},
    {"key": "challengeAdherence", "weight": 5}
  ]
}
```

## Evolução da rubrica

Rubricas usadas por avaliações publicadas não devem ser alteradas retroativamente.

Nova configuração = nova versão.
