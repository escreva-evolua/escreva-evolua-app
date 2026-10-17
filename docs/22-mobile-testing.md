# 22 — Mobile Testing Strategy

**Status:** estratégia proposta para o futuro cliente Flutter. Não pressupõe que o app esteja implementado.

## Objetivo

Proteger os fluxos críticos em Android e iOS com testes proporcionais ao risco, sem exigir testes E2E para toda alteração visual.

## Pirâmide de testes

### Unitários

Usar para:
- validações locais;
- transformações de estado;
- regras de apresentação isoladas;
- parsing e serialização;
- lógica de formulários.

### Widget tests

Usar para:
- editor e componentes de feedback;
- estados loading/empty/error;
- validação de formulário;
- acessibilidade básica e interações;
- responsividade por dimensões de tela.

### Integration tests

Usar para fluxos que atravessam navegação, estado e serviços:
- autenticação;
- selecionar desafio;
- escrever e salvar rascunho;
- submeter texto;
- visualizar avaliação;
- reescrever e comparar versões;
- upload de imagem e confirmação de OCR.

### Testes manuais em dispositivos

Manter uma matriz mínima de dispositivos físicos/emuladores para:
- teclado virtual e área útil;
- retomada após app em background;
- conectividade instável;
- permissões de câmera/fotos;
- leitores de tela;
- diferentes tamanhos de tela.

## Fluxos prioritários

1. Responsável cria ou acessa perfil autorizado.
2. Escritor abre desafio e escreve.
3. Rascunho é preservado em erro recuperável.
4. Submissão recebe estado claro enquanto a avaliação está pendente.
5. Resultado e feedback correspondem à versão submetida.
6. Foto é enviada, transcrição é confirmada e erros podem ser corrigidos.
7. A pessoa consulta uma avaliação anterior sem que ela seja recalculada silenciosamente.

## Confiabilidade e economia

- Não executar toda a suíte mobile em mudanças de documentação.
- Para alteração isolada de lógica, executar unit tests relevantes.
- Para alteração de widget, executar widget tests relacionados.
- Para alteração de jornada, executar integration test correspondente.
- Evitar sleeps fixos; aguardar estados observáveis.
- Usar dados sintéticos, sem textos reais ou dados pessoais de crianças em fixtures.
- Registrar plataforma, versão, comando, resultado e limitações.

## Critérios de conclusão

Uma jornada crítica não está validada apenas porque compila. Deve haver evidência do nível de teste apropriado, e limitações de dispositivos/ambientes devem ser registradas.
