# Backend

Base executável do monólito modular, ainda sem módulos de negócio ou serviços externos.

## Pré-requisitos

- JDK 25 (a compilação falha claramente se outra versão for usada).
- Acesso à internet no primeiro build para baixar a distribuição Maven e dependências.

O Maven Wrapper 3.3.4 executa Apache Maven 3.9.16. O backend usa Quarkus 3.40.1, manutenção fixada do stream 3.40 LTS.

## Build e testes

Na raiz do backend, execute o mesmo comando da CI:

```sh
./mvnw --batch-mode --no-transfer-progress verify
```

Esse comando compila a aplicação, executa testes e empacota o artefato. Não requer MongoDB, Redis, RabbitMQ, credenciais ou conexão a provedores de IA.

## Execução local

```sh
./mvnw quarkus:dev
```

A aplicação inicia em `http://localhost:8080`. Os health checks padrão são:

- Liveness: `GET http://localhost:8080/q/health/live`
- Readiness: `GET http://localhost:8080/q/health/ready`

Após a inicialização, ambos retornam HTTP 200 e estado `UP`.
