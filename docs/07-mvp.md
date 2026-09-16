# 07 — MVP

## Escopo

### Público

Primário: 7–17 anos, sempre respeitando a regra de responsável para menores de 16.

Secundário: adultos, com experiência própria.

### Incluído

- cadastro/autenticação;
- responsável;
- perfil de escritor;
- data de nascimento;
- atualização automática no aniversário;
- interesses;
- nível;
- frequência;
- geração de desafios;
- anti-repetição;
- editor;
- upload de fotos;
- OCR/visão;
- confirmação da transcrição;
- avaliação 0–100;
- coesão;
- coerência;
- aderência ao desafio;
- criatividade;
- feedback;
- histórico;
- evolução;
- rubrica configurável por admin;
- RabbitMQ;
- AI Gateway;
- DeepSeek primário;
- fallback OpenAI/Codex inicialmente previsto;
- observabilidade básica.

## Fora do MVP

- professores/turmas;
- comunidade pública;
- ranking público;
- marketplace;
- publicação de livros;
- recursos sociais;
- conteúdo escolar estruturado;
- integrações institucionais.

## Core Loop

```text
Novo desafio
→ escrever ou fotografar
→ confirmar texto
→ avaliar
→ receber nota
→ receber feedback
→ acompanhar evolução
→ próximo desafio
```

## MVP Success Criteria

1. Usuário consegue concluir um desafio ponta a ponta.
2. Desafios não se repetem semanticamente.
3. Avaliação é reproduzível dentro da mesma versão de rubrica/modelo.
4. Foto manuscrita pode virar texto confirmável.
5. Falha do LLM não perde a submissão.
6. Responsável consegue visualizar evolução do menor.
7. Sistema registra métricas suficientes para validar retenção.
