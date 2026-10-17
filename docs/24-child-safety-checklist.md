# 24 — Child Safety and Privacy Checklist

**Status:** checklist de design para discovery/specification; não é parecer jurídico nem política final.

A fonte normativa do projeto continua sendo `docs/15-security-lgpd.md`. Este checklist traduz seus princípios em perguntas de projeto e critérios para especificações.

## Cadastro e perfis

- [ ] O fluxo respeita a regra de idade documentada?
- [ ] A relação entre responsável e perfil do menor é verificada e autorizada?
- [ ] O backend valida autorização em cada operação sensível, não apenas na interface?
- [ ] A aplicação coleta apenas dados necessários?
- [ ] A política de consentimento e sua evidência foram definidas antes do lançamento?

## Escrita e conteúdo

- [ ] Textos são privados por padrão?
- [ ] O produto evita compartilhamento público de textos de menores como padrão?
- [ ] Conteúdo de usuário é tratado como dado não confiável, inclusive quando contém instruções dirigidas à IA?
- [ ] Os fluxos de risco/safety têm política aprovada e comportamento de fallback?
- [ ] O feedback evita diagnóstico, julgamento pessoal e inferências psicológicas não justificadas?

## IA e terceiros

- [ ] O contexto enviado ao provedor foi minimizado e, quando possível, pseudonimizado?
- [ ] Dados de identidade e credenciais são excluídos dos prompts?
- [ ] A retenção e o uso de dados por cada provedor foram avaliados?
- [ ] Existe tratamento para saída inválida, indisponibilidade e conteúdo inadequado?
- [ ] Logs e métricas não armazenam textos completos ou dados pessoais sem necessidade justificada?

## Imagens e OCR

- [ ] Há validação de formato, tamanho e conteúdo de arquivo?
- [ ] Há prevenção/mitigação de upload malicioso?
- [ ] A pessoa pode revisar e corrigir a transcrição?
- [ ] O desenho considera nomes, escola, endereço e outros dados visíveis na imagem?
- [ ] A retenção e a exclusão da imagem original estão definidas?

## Autorização e isolamento

- [ ] Testes cobrem acesso cruzado entre perfis e responsáveis?
- [ ] Operações administrativas são autorizadas e auditadas?
- [ ] Os registros de auditoria evitam segredos e dados excessivos?
- [ ] A exclusão/exportação de dados e o ciclo de vida de contas estão especificados?

## Lançamento

- [ ] Política de retenção, consentimento, exclusão, exportação, analytics, cookies e terceiros foi aprovada?
- [ ] A avaliação jurídica aplicável foi concluída?
- [ ] A análise de ameaças foi revisada após mudanças relevantes?
- [ ] Existe processo de resposta a incidentes?
- [ ] Riscos residuais têm responsável e decisão explícita?

Qualquer item não resolvido deve ser registrado como risco ou decisão pendente; não deve ser silenciosamente considerado atendido.
