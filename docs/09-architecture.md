# 09 — Architecture

## Decisão

O MVP será um **Modular Monolith**, não uma arquitetura de microserviços.

### O que isso significa

É **uma aplicação implantável como uma unidade**, mas internamente dividida em módulos de negócio com fronteiras explícitas.

Não significa “um projeto bagunçado com todas as classes misturadas”.

### Estrutura conceitual

```text
                    API
                     │
             ┌───────▼────────┐
             │ Application     │
             │ Use Cases       │
             └───────┬────────┘
                     │
        ┌────────────┼────────────┐
        ▼            ▼            ▼
   Challenges     Writing     Evaluation
        │            │            │
        └────────────┼────────────┘
                     ▼
                Domain Ports
                     │
       ┌─────────────┼─────────────┐
       ▼             ▼             ▼
   Mongo Adapter  AI Adapter   Rabbit Adapter
```

## Módulos

```text
identity
profiles
challenges
writing
evaluation
progress
gamification
notifications
ai
administration
```

Cada módulo deve possuir:
- domínio;
- casos de uso;
- portas;
- adapters quando necessários;
- testes.

## Por que não microserviços agora?

Porque ainda temos:
- baixa certeza sobre limites de escala;
- domínio em descoberta;
- poucos fluxos críticos;
- custo operacional desnecessário;
- necessidade de evoluir rapidamente.

O modular monolith permite separar responsabilidades sem pagar inicialmente o custo operacional de uma arquitetura distribuída.

## Quando extrair um módulo

Somente quando houver motivo mensurável, por exemplo:
- escala muito diferente;
- ciclo de deploy independente;
- requisito de segurança isolado;
- equipe independente;
- disponibilidade independente;
- carga de IA muito superior à API.

O primeiro candidato natural a processamento independente é o worker de IA, não necessariamente um microserviço de domínio.

## Persistência

Domínio/application define interfaces como:

```java
interface WriterRepository {
    Optional<Writer> findById(WriterId id);
    Writer save(Writer writer);
}
```

O adapter Mongo implementa essa interface.

Futuramente:

```text
MongoWriterRepository
PostgresWriterRepository
```

sem alteração do domínio.

## Comunicação assíncrona

RabbitMQ será usado para:
- avaliação;
- geração;
- ingestão de imagens;
- notificações;
- retries.

## Frontends

```text
Angular Web
      │
Flutter Android/iOS
      │
      ▼
REST API
```

## Infraestrutura inicial

```text
Internet
  ↓
Ingress/API Gateway
  ↓
Quarkus API
  ├── MongoDB
  ├── Redis
  └── RabbitMQ
       └── AI Workers
             ├── DeepSeek
             └── Codex/OpenAI
```

## Java

Adotar **Java 25 LTS**.

Java 25 foi lançado em setembro de 2025 e é LTS. Quarkus 3.31 adicionou suporte completo a Java 25. Para produção, usar o stream LTS do Quarkus; neste momento, Quarkus 3.40 é o LTS recomendado.

## Regra arquitetural

Nenhum caso de uso pode:
- instanciar SDK de DeepSeek;
- chamar Mongo diretamente;
- publicar diretamente no RabbitMQ;
- depender de Angular/Flutter;
- conhecer detalhes de armazenamento de arquivo.

Tudo entra por portas.
