# 15 — Security, Children and Privacy

## Regra de idade

### Menor de 16

Somente dentro de perfil de responsável maior de 18 anos.

### 16–17

Pode haver fluxo específico de cadastro, sujeito à análise jurídica e de produto.

### 18+

Conta individual.

A regra definitiva deve ser validada juridicamente antes do lançamento.

## Segurança de conteúdo

Desafios para menores devem passar por:
- classificação etária;
- safety validation;
- política de conteúdo;
- validação pós-geração.

## Dados de crianças

Princípios:
- minimização;
- menor privilégio;
- isolamento por perfil;
- logs de acesso;
- auditoria;
- criptografia em trânsito;
- segredos fora do código;
- exclusão futura conforme política.

## Fotos

Imagens de manuscritos podem conter:
- nome;
- escola;
- endereço;
- outras informações pessoais.

O pipeline deve considerar isso.

## LLM

Nunca enviar mais dados pessoais do que o necessário.

Preferir contexto pseudonimizado:

```text
age=11
level=intermediate
language=pt-BR
```

em vez de dados de identidade.

## Privacy by Design

A política completa de:
- retenção;
- consentimento;
- exclusão;
- exportação;
- uso para treinamento;
- analytics;
- cookies;
- terceiros;

fica explicitamente como decisão futura antes do beta.

## Auditoria

Registrar:
- alteração de rubrica;
- alteração de policy;
- alterações de perfil de menor;
- avaliações;
- acessos administrativos;
- chamadas de IA relevantes.

## Threat Model inicial

Principais riscos:
- acesso indevido a textos;
- acesso cruzado entre perfis;
- prompt injection em textos enviados;
- conteúdo inadequado gerado;
- vazamento de segredos;
- upload malicioso;
- abuso de API;
- custos de IA;
- falsificação de idade.
