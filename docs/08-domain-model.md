# 08 — Domain Model

## Aggregates / Core Concepts

### Account
Identidade e autenticação.

### ResponsibleProfile
Conta adulta que pode administrar perfis de menores.

### WriterProfile
Perfil de escrita de uma pessoa.

Principais atributos:
- birthDate;
- ageSnapshot;
- level;
- interests;
- preferences;
- frequency;
- skills.

### AgeProfile
Versão das regras esperadas para uma faixa etária.

### Challenge
Desafio criado, validado e versionado.

### ChallengeDelivery
Registro de qual desafio foi entregue a qual escritor.

### WritingSubmission
Uma produção enviada pelo usuário.

### WritingVersion
Versão específica de uma produção.

### ImageSubmission
Imagem associada à produção.

### Transcription
Texto extraído de imagens, com confiança e status de confirmação.

### Evaluation
Resultado de uma avaliação.

### Rubric
Conjunto de critérios e pesos.

### RubricVersion
Versão imutável da rubrica.

### SkillProgress
Estado de evolução de uma habilidade.

### Achievement
Conquista.

### Streak
Sequência de prática.

## Relações

```text
Account
 └── ResponsibleProfile
      └── WriterProfile
           ├── ChallengeDelivery
           ├── WritingSubmission
           │    ├── WritingVersion
           │    └── ImageSubmission
           ├── Evaluation
           ├── SkillProgress
           ├── Achievement
           └── Streak
```

## Portabilidade

O domínio não deve importar classes do MongoDB, RabbitMQ ou SDKs de LLM.
