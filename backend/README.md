# Backend

Backend Java 25 com Spring Boot 4.1.2, organizado como monólito modular e com persistência MongoDB.

## Pré-requisitos

- JDK 25.
- Docker para os testes de integração MongoDB (ou MongoDB 8 disponível em `localhost:27017`).

O Maven Wrapper executa o build e os testes, sem exigir uma instalação local do Maven.

## Build e testes

Na raiz do backend, execute o mesmo comando da CI:

```sh
./mvnw --batch-mode --no-transfer-progress verify
```

## Execução local

```sh
./mvnw spring-boot:run
```

A aplicação inicia em `http://localhost:8080`. Liveness e readiness são expostos em `/actuator/health/liveness` e `/actuator/health/readiness`.

## Configuração de identidade

O issuer, client autorizado e URI Mongo devem ser configurados por ambiente. Não versione credenciais.

| Variável | Uso |
|---|---|
| `MONGODB_CONNECTION_STRING` | URI do MongoDB; por padrão usa `mongodb://localhost:27017/escreva-evolua`. |
| `MONGODB_DATABASE` | Banco da aplicação. |
| `COGNITO_ISSUER` | Issuer OIDC exato do User Pool, usado para descoberta/JWKS e validação. |
| `COGNITO_APP_CLIENT_ID` | Client ID autorizado para access tokens da API. |
| `COGNITO_USER_INFO_URL` | Endpoint HTTPS Cognito `/oauth2/userInfo`. |
| `COGNITO_API_AUDIENCE` | Opcional; configurar quando o User Pool emitir `aud` por resource binding. |
| `AWS_REGION` | Região do User Pool. |
| `COGNITO_USER_POOL_ID` | Pool onde o backend executa `AdminCreateUser`. |
| `FRONTEND_ORIGINS` | Origens exatas separadas por vírgula. |
| `IDENTITY_PUBLIC_AUTH_RATE_LIMIT_PER_MINUTE` | Limite por endereço remoto e endpoint público de convite/cadastro; padrão 10 por minuto. |

Configure no User Pool Managed Login com Authorization Code + PKCE, self-service sign-up desabilitado, escopos `openid email`, envio de senha temporária e verificação de e-mail obrigatória. A role do backend precisa somente das ações `cognito-idp:AdminCreateUser` e `cognito-idp:AdminGetUser` no User Pool configurado. O SDK usa a cadeia padrão de credenciais AWS; não use access keys no repositório.

O backend aceita somente access tokens com assinatura/JWKS, issuer, validade, `token_use=access`, `sub` e `client_id` válidos. `aud` só é exigido quando uma audience de recurso está configurada. A finalização consulta UserInfo pelo access token validado e confirma `sub`, `email` e `email_verified` antes da associação local.

Os endpoints públicos de validação de convite e início do cadastro têm limite em memória por instância, por endereço remoto e por rota, com resposta HTTP 429 e `Retry-After`. O limite deve ser complementado por rate limiting na borda antes do beta, especialmente quando houver múltiplas instâncias. A aplicação não confia em `X-Forwarded-For`; só configure encaminhamento de proxy confiável na infraestrutura quando a borda estiver definida.

Endpoints de identidade:

- `POST /api/v1/auth/invitations/validate` — valida e-mail/código sem expor o motivo da recusa.
- `POST /api/v1/auth/register` — provisiona por convite via AdminCreateUser e retorna instrução genérica HTTP 202.
- `POST /api/v1/auth/register/finalize` — access token + código + nome de exibição; confirma e-mail e consome o convite.
- `GET /api/v1/auth/me` — conta e perfil derivados exclusivamente da identidade autenticada.

As operações administrativas ficam disponíveis pela CLI do artefato:

```sh
INVITATION_OPERATOR=operador-1 java -jar target/backend-1.0.0-SNAPSHOT.jar invitation-create --email adulto@example.com
INVITATION_OPERATOR=operador-1 java -jar target/backend-1.0.0-SNAPSHOT.jar invitation-revoke --id <uuid>
INVITATION_OPERATOR=operador-1 java -jar target/backend-1.0.0-SNAPSHOT.jar account-status --account-id <uuid> --action BLOCK
```

O comando de criação imprime o código em claro uma única vez. Distribua-o por canal aprovado e não o copie para logs, tickets ou histórico do terminal compartilhado. A validade é de 30 dias. Auditoria registra evento, operador, ID do recurso e horário; não registra código nem e-mail. As leases de cadastro expiram em dois minutos e permitem retry após falha local.
