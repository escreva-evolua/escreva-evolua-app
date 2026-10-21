# SPEC-001 — Backend Foundation

- **Status:** Aprovada
- **Tipo:** fundação técnica do MVP
- **Módulo:** backend
- **Branch de referência:** `main`
- **Base de produto:** `README.md`, `docs/06-non-functional-requirements.md`, `docs/07-mvp.md`, `docs/09-architecture.md`
- **Decisões relacionadas:** ADR-001 (Java/Quarkus), ADR-002 (Modular Monolith), ADR-003 (Persistence Portability)

## 1. Contexto e problema

O repositório contém decisões de produto, arquitetura e segurança, mas ainda não contém código de aplicação nem uma ferramenta de build executável. Antes de implementar funcionalidades de negócio, precisamos de uma base de backend que compile, execute, seja testável em CI e estabeleça convenções que não contrariem a arquitetura modular definida.

Esta spec trata somente da fundação executável. Ela não tenta entregar antecipadamente o MVP nem introduzir integrações externas antes de existir uma feature aprovada que dependa delas.

## 2. Objetivo

Criar uma aplicação backend inicial em Java/Quarkus, executável localmente e em CI, que estabeleça:

1. estrutura inicial do backend como parte de um monólito modular;
2. build reproduzível com Maven Wrapper;
3. configuração segura por ambiente;
4. endpoints de liveness e readiness;
5. testes de integração mínimos e testes unitários quando houver comportamento próprio isolável;
6. validação automática do build e testes em pull requests.

## 3. Escopo

### Incluído

- Projeto backend em `backend/`, sem mover os documentos atuais da raiz.
- Java 25 LTS.
- Quarkus no stream 3.40 LTS, fixado e documentado.
- Apache Maven Wrapper 3.3.4, versão estável mais recente verificada em 17/10/2026, versionado no repositório. O projeto Apache não publica um canal LTS separado para o Wrapper.
- Configuração mínima da aplicação e perfis de execução suportados pelo Quarkus.
- Health checks de liveness e readiness.
- Testes automatizados que não dependam de MongoDB, Redis, RabbitMQ ou credenciais de LLM.
- Workflow de GitHub Actions que execute o build e os testes em push para `main` e pull requests direcionados a `main`.
- Procedimento local para executar, a cada implementação de feature ou correção, o mesmo comando de build/testes da CI antes de abrir ou atualizar o pull request.
- Instruções de desenvolvimento local no README principal ou em `backend/README.md`.
- Registro da decisão de versão do Quarkus e alinhamento entre ADR-001, README e configuração real.

### Fora do escopo

- Cadastro, login, autorização, responsável ou perfil de escritor.
- Desafios, editor de texto, submissões, OCR e upload de imagens.
- Motor de avaliação, rubricas e geração de feedback.
- Chamadas a DeepSeek, OpenAI/Codex ou qualquer outro LLM.
- MongoDB, Redis, RabbitMQ, storage externo e migrações.
- API de negócio em `/api/v1`.
- Frontend Angular, aplicativo Flutter, design system e deploy de produção.
- Docker/Compose, Kubernetes/OpenShift, observabilidade avançada e infraestrutura de cloud.

Esses itens dependem de specs posteriores. A fundação não deve incluir extensões ou abstrações sem uso concreto nesta entrega.

## 4. Decisão de versão

O ADR-001 e o README fixam Quarkus 3.40 LTS como o stream a ser usado no projeto, junto com Java 25 LTS. Essa decisão já está registrada e alinhada entre os documentos aceitos.

O stream 3.40 LTS está fixado. A versão de manutenção compatível deve ser fixada no build e registrada no README durante a implementação.

O Apache Maven Wrapper não possui canal ou classificação LTS publicada pelo projeto Apache. A versão estável mais recente encontrada na fonte oficial em 17/10/2026 é 3.3.4; esta é a versão do Wrapper a ser usada e fixada. A distribuição Maven executada pelo Wrapper deve ser Apache Maven 3.9.16, versão indicada no guia de testes da linha Quarkus 3.40, e ficar fixada em `maven-wrapper.properties`.

Não atualizar a versão em código sem alinhar primeiro os documentos de decisão aceitos.

## 5. Requisitos

### FR-FOUND-001 — Build reproduzível

O backend deve compilar e executar os testes por meio do Maven Wrapper, sem exigir que cada desenvolvedor instale uma versão global específica do Maven.

### FR-FOUND-002 — Versões explícitas

A versão de Java e a versão de Quarkus usadas pelo build devem estar fixadas e documentadas. A CI deve falhar de forma clara quando for usada uma versão de Java incompatível.

### FR-FOUND-003 — Execução local

Um desenvolvedor deve conseguir iniciar o backend seguindo somente os comandos documentados, sem configurar serviços externos ou fornecer segredos.

### FR-FOUND-004 — Health checks

A aplicação deve expor os endpoints padrão do Quarkus SmallRye Health para liveness e readiness. Ambos devem retornar estado saudável quando a aplicação tiver inicializado corretamente, sem verificar dependências externas que ainda não fazem parte desta spec.

### FR-FOUND-005 — Testabilidade

A base deve incluir testes de integração HTTP para liveness e readiness. Testes unitários devem cobrir lógica própria da aplicação quando houver comportamento isolado relevante; não criar lógica ou testes artificiais apenas para cumprir uma contagem. Nesta fundação, que não define lógica de negócio própria, os testes de integração dos endpoints são obrigatórios e um teste unitário não é obrigatório. Todos os testes devem ser executáveis em ambiente limpo e na CI.

### FR-FOUND-006 — CI

O workflow de CI deve executar o build e a suíte de testes em push para `main` e em pull requests cujo branch de destino seja `main`. Falha no build ou em teste deve tornar a execução malsucedida. A documentação deve indicar o comando local que reproduz essas verificações; a cada implementação de feature ou correção, esse comando deve ser executado localmente antes do pull request ou de sua atualização. Cada spec/implementação deve registrar evidência de que o CI aplicável terminou sem falhas; não concluir a entrega enquanto houver falha sem resolução.

### FR-FOUND-007 — Configuração segura

Nenhuma credencial ou segredo deve ser incluído no repositório. Configurações sensíveis futuras devem ser fornecidas por ambiente ou mecanismo de secrets da plataforma, não por valores reais versionados.

### FR-FOUND-008 — Fronteiras arquiteturais

A estrutura e as instruções devem reforçar a decisão de Modular Monolith: módulos organizados por domínio, dependências explícitas e domínio/casos de uso sem dependência direta de banco, mensageria, LLM ou UI. Não criar módulos vazios apenas para antecipar funcionalidades.

### FR-FOUND-009 — Independência de serviços

A aplicação e os testes desta spec devem iniciar sem MongoDB, Redis, RabbitMQ, provedores de IA ou credenciais externas.

### FR-FOUND-010 — Documentação operacional

A documentação deve mostrar pré-requisitos, comando de build/teste, comando de execução local, versão de Java/Quarkus e endpoints de health checks. Os comandos documentados devem ser verificados durante a implementação.

## 6. Estrutura conceitual esperada

A estrutura física exata pode seguir o gerador oficial Quarkus e as convenções validadas na implementação, mantendo o backend separado da documentação atual:

```text
/
├── backend/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── .mvn/wrapper/
│   ├── src/main/java/
│   ├── src/main/resources/application.properties
│   ├── src/test/java/
│   └── README.md
├── .github/workflows/backend-ci.yml
├── adrs/
├── docs/
└── specs/
```

A convenção de pacote Java será `br.com.escrevaevolua`, usada consistentemente nos fontes e testes, alinhada ao domínio reverso do projeto. O namespace é definido uma única vez nesta spec e não deve ser redefinido durante a implementação. Não criar classes ou interfaces de domínio sem comportamento requerido nesta spec.

## 7. Critérios de aceitação

- **AC-001:** o Maven Wrapper executa o build em um ambiente limpo com Java 25.
- **AC-001a:** os arquivos do Maven Wrapper usam a versão 3.3.4, a mais recente estável verificada na fonte oficial em 17/10/2026, e configuram a distribuição Apache Maven 3.9.16. A documentação não classifica o Wrapper como LTS, pois o Apache não publica essa classificação.
- **AC-002:** o comando de build e testes termina com sucesso sem exigir serviços externos ou segredos.
- **AC-003:** o backend inicia localmente com as instruções documentadas.
- **AC-004:** o endpoint de liveness retorna HTTP 200 e estado `UP` após inicialização.
- **AC-005:** o endpoint de readiness retorna HTTP 200 e estado `UP` após inicialização, sem dependências externas nesta fase.
- **AC-006:** testes de integração fazem requisições HTTP para liveness e readiness e verificam status HTTP e estado `UP`. Não há teste unitário artificial para componentes sem lógica própria.
- **AC-007:** a CI executa build e testes em push para `main` e em pull requests direcionados a `main`, falhando se qualquer etapa falhar.
- **AC-007a:** o comando local documentado reproduz as etapas de build/testes da CI e é executado a cada feature/correção; cada spec/implementação registra CI aplicável sem falhas antes de ser concluída.
- **AC-008:** Java e Quarkus estão fixados no build e documentados de forma consistente em README e ADR-001.
- **AC-009:** não há credenciais, tokens, chaves de API ou dados pessoais reais nos arquivos adicionados.
- **AC-010:** a fundação não implementa endpoints de negócio nem integrações fora do escopo.
- **AC-011:** não há dependência direta de MongoDB, Redis, RabbitMQ ou SDK de IA no código desta spec.
- **AC-012:** `git diff --check` passa e o diff não inclui arquivos gerados de build ou IDE.

## 8. Casos de erro e comportamento esperado

| Situação | Comportamento esperado |
|---|---|
| Java local incompatível | Build falha com mensagem identificável da versão exigida. |
| Compilação ou teste falha | Comando e CI terminam com código diferente de zero. |
| Serviço externo não configurado | Não impede iniciar ou testar a aplicação desta spec, pois nenhum serviço externo está no escopo. |
| Segredo ausente | Não há segredo obrigatório nesta fase; nenhum fallback para credenciais hardcoded. |
| Health check antes da inicialização completa | Estado não saudável ou resposta apropriada ao estado real do processo, sem declarar readiness prematuramente. |

## 9. Estratégia de testes

- Testar compilação e empacotamento pelo Maven Wrapper.
- Adicionar testes de integração Quarkus que consultem liveness e readiness via HTTP e verifiquem status e payload.
- Adicionar testes unitários apenas para comportamento isolado implementado pelo projeto; não introduzir classes ou lógica artificial para gerar cobertura nesta fundação.
- Executar localmente, em cada feature/correção, o comando de build/testes usado pela CI e registrar evidência por spec/implementação.
- Confirmar que o workflow roda apenas em push para `main` e pull requests com destino `main`.
- Confirmar que os testes não tentam conectar a serviços externos.
- Revisar arquivos versionados em busca de segredos e configurações locais.
- Revisar as fronteiras arquiteturais em conjunto com ADR-002.

## 10. Dependências e documentos relacionados

- ADR-001 — Java + Quarkus 3.40 LTS.
- ADR-002 — Modular Monolith.
- ADR-003 — Portabilidade da persistência; sem adapter de banco nesta spec.
- `docs/06-non-functional-requirements.md` — portabilidade, confiabilidade, segurança e manutenção.
- `docs/09-architecture.md` — módulos, portas/adapters e infraestrutura futura.
- `.darp/lifecycle.md` — sequência spec, ADR, plano, tarefas, implementação e gates.
- `.darp/governance/quality-gates.md` — build, testes, documentação, arquitetura, compatibilidade e release notes.

## 11. Definition of Done

- Todos os critérios AC-001 a AC-012 foram verificados com evidências.
- A decisão de Quarkus está registrada e os documentos relacionados não se contradizem.
- Build, testes e CI passam.
- Revisões de arquitetura e documentação não identificam bloqueadores abertos.
- Quality gates aplicáveis foram registrados como `PASS` ou `WARNING` explicitamente aceito.
- Nenhuma funcionalidade de negócio ou integração fora do escopo foi incluída.

## 12. Decisões explicitamente adiadas

- Estrutura completa dos módulos de domínio.
- Contrato de erros HTTP para endpoints de negócio.
- Autenticação e autorização.
- Configuração de MongoDB, Redis e RabbitMQ.
- Integração do AI Gateway e versão de prompts.
- Empacotamento de container e implantação.
- Estratégia detalhada de observabilidade.
