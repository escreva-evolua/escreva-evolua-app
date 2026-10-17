# 21 — Design System Principles

**Status:** princípios iniciais; tokens visuais e componentes concretos dependem de decisão de design.

## Objetivo

Criar uma experiência de escrita acolhedora e clara para crianças, adolescentes e adultos, sem tornar a interface infantilizada nem parecer um corretor escolar.

## Princípios

1. **A escrita é o foco:** o editor de texto deve ter hierarquia visual clara e distrações reduzidas.
2. **Feedback sem punição:** notas não devem dominar visualmente a experiência; destacar progresso e ações possíveis.
3. **Idade define adequação, não identidade visual infantil:** adultos precisam de uma experiência madura e crianças precisam de clareza e segurança.
4. **Progresso individual:** enfatizar evolução pessoal, reescrita e habilidades desenvolvidas; evitar rankings públicos como padrão.
5. **Acessibilidade desde o início:** contraste adequado, navegação por teclado, foco visível, textos legíveis e mensagens não dependentes apenas de cor.
6. **Estados explícitos:** loading, vazio, erro, sucesso e indisponibilidade de IA devem ser compreensíveis e recuperáveis.
7. **Privacidade visível:** deixar claro quando um texto será enviado para avaliação e evitar exposição de dados de menores.
8. **Mobile-first para escrita curta, responsivo para escrita longa:** preservar conforto de leitura e edição em telas pequenas e grandes.

## Componentes prioritários

- editor de escrita;
- cartão de desafio;
- feedback da avaliação;
- comparação entre versões;
- indicador de progresso individual;
- seletor de interesses/faixa de dificuldade;
- confirmação e estado de upload de imagem;
- controles de responsável;
- mensagens de erro e indisponibilidade.

## Regras de interação

- Preservar rascunho diante de falha recuperável sempre que tecnicamente possível.
- Não perder texto ao navegar para ver feedback.
- Não iniciar avaliação sem ação/consentimento explícito do usuário quando aplicável.
- Explicar erros de OCR e permitir correção da transcrição.
- Evitar animações que atrapalhem leitura, foco ou acessibilidade.

## Decisões ainda necessárias

- identidade visual e tipografia;
- paleta e tokens de cor;
- biblioteca de componentes;
- regras de densidade e espaçamento;
- padrões específicos por faixa etária;
- requisitos de acessibilidade e metas de conformidade.

Não inventar tokens ou biblioteca de UI durante uma implementação isolada. Propor a decisão em uma especificação ou ADR de design quando houver evidência suficiente.
