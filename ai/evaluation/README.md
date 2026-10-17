# AI Evaluation Cases

Os arquivos YAML deste diretório são casos iniciais para uma futura suíte de avaliação. São dados de teste, não prompts de produção.

## Como usar

1. Transforme cada caso em teste executável quando a infraestrutura de IA estiver especificada.
2. Use apenas dados sintéticos.
3. Mantenha cada caso pequeno para reduzir custo e facilitar diagnóstico.
4. Não dependa de correspondência literal da resposta do modelo; avalie propriedades e critérios.
5. Registre provider, model, prompt version, rubric version, parâmetros, custo e resultado.

## Camadas

- `smoke`: poucos casos, baixo custo, para mudanças locais.
- `regression`: conjunto representativo antes de promover prompts/modelos.
- `safety`: casos de segurança e limites de comportamento.
- `human_review`: avaliação humana necessária para dimensões subjetivas.

## Estado inicial

Os casos aqui são candidatos e precisam ser revisados por produto/engenharia antes de serem tratados como critérios de release. Os thresholds numéricos devem ser definidos depois de obter baseline com exemplos calibrados.
