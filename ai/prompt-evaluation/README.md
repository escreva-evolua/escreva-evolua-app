# Prompt Evaluation

Este diretório define como comparar versões de prompts sem transformar toda alteração em uma avaliação cara.

## Quando executar

Execute uma avaliação quando houver mudança relevante em:
- prompt;
- modelo/provedor;
- parâmetros de geração;
- schema de saída;
- rubrica;
- política de safety;
- roteamento/fallback.

Não execute uma suíte LLM completa em mudanças de documentação ou código sem impacto nas tarefas de IA.

## Processo

1. Selecione casos da suíte de acordo com o risco.
2. Fixe provider, model, parâmetros e versões de prompt/rubrica.
3. Execute a versão baseline e a candidata com as mesmas entradas.
4. Valide automaticamente schema, limites numéricos e invariantes.
5. Use avaliação humana para criatividade, adequação etária e utilidade.
6. Compare qualidade, falhas, latência e custo.
7. Registre a decisão e os riscos residuais.

## Comparação justa

- Alterar um fator por vez quando possível.
- Não comparar respostas produzidas com rubricas diferentes sem explicitar o efeito.
- Não usar uma única média para esconder regressões críticas.
- Não promover uma versão que viole um bloqueador de segurança mesmo que a média geral melhore.

## Economia

- Smoke: poucos casos curtos.
- Regression: casos representativos.
- Safety: casos de risco.
- Human review: amostra pequena, estratificada e revisada por avaliadores.
- Definir limite de custo antes da execução.
- Guardar metadados e resultados, não dados pessoais desnecessários.
