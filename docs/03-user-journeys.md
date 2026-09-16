# 03 — User Journeys

## J1 — Primeiro acesso do menor

1. Responsável cria conta.
2. Responsável informa dados mínimos.
3. Cria perfil do menor.
4. Informa data de nascimento.
5. Seleciona interesses.
6. Seleciona frequência desejada.
7. Sistema calcula faixa etária e nível inicial.
8. Sistema apresenta primeiro desafio.
9. Menor escreve ou envia foto.
10. Sistema avalia.
11. Sistema apresenta nota e feedback.
12. Resultado entra no histórico.

## J2 — Desafio recorrente

```text
Notificação → Abrir desafio → Ler → Escrever → Enviar → Avaliação → Evolução
```

A frequência é definida pelo usuário/responsável e não deve ser fixa pelo produto.

## J3 — Texto manuscrito

1. Usuário seleciona “Enviar foto”.
2. Fotografa uma ou mais páginas.
3. Sistema valida qualidade da imagem.
4. Pipeline de ingestão faz OCR/visão.
5. Se a confiança for baixa, solicita nova foto.
6. Texto extraído é apresentado para confirmação.
7. Usuário corrige eventuais erros de transcrição.
8. Texto confirmado segue para avaliação.
9. Imagens originais permanecem sujeitas à política de retenção.

## J4 — Reescrita

```text
Avaliação inicial
↓
Sugestões de melhoria
↓
Melhorar texto
↓
Nova versão
↓
Nova avaliação
↓
Comparação
```

O produto deve destacar evolução, não punição.

## J5 — Aniversário

Quando a data de nascimento indicar mudança de idade:

1. Sistema recalcula faixa etária.
2. Mantém histórico anterior.
3. Atualiza expectativas/rubrica aplicável.
4. Atualiza geração de desafios.
5. Registra mudança de versão do perfil etário.

O histórico nunca deve ser sobrescrito.

## J6 — Responsável acompanha

Responsável acessa dashboard e visualiza:

- frequência;
- textos concluídos;
- evolução por habilidade;
- conquistas;
- tendências.

Por padrão, evitar ranking entre irmãos.

## J7 — Adulto

Cadastro → perfil de escritor → interesses → frequência → desafio → escrita → avaliação → evolução.

A experiência não deve parecer infantil.
