# AuthAPI - API de Autenticacao com JWT, Refresh Rotation e Fluxos de Seguranca

## 1. Titulo do Projeto
AuthAPI - API REST para autenticacao, autorizacao basica por papel (role) e gestao de sessao com refresh token rotativo.

## 2. Visao Geral
A AuthAPI foi desenvolvida em Spring Boot para centralizar autenticacao de usuarios por sistema, com foco em seguranca operacional no dia a dia:

- Login em 2 etapas (senha + codigo por email).
- Access token JWT para chamadas autenticadas.
- Refresh token com rotacao (um uso por token) e deteccao de reuse/tampering.
- Logout com revogacao dos refresh tokens do usuario.
- Fluxo de esqueci senha com challenge temporario.
- Bloqueio progressivo apos falhas de login.
- Historico de senha para impedir reutilizacao.
- Tratamento padronizado de erros (`ErrorDTO`) e logs estruturados com `X-Request-Id`.

## 3. Funcionalidades
Baseado no codigo atual: (03/04/2026)

- Cadastro de usuario com validacao de email unico e politica de senha forte.
- Login com validacao de credenciais + envio de codigo de verificacao por email.
- Confirmacao de login por codigo para emissao de `accessToken` + `refreshToken`.
- Refresh token com:
  - armazenamento apenas do hash do segredo do token;
  - rotacao a cada renovacao;
  - deteccao de token adulterado/reuso;
  - revogacao de todas as sessoes do usuario em caso de incidente.
- Logout invalidando todos os refresh tokens do usuario autenticado.
- Alteracao de senha em 2 etapas (senha atual + codigo por email).
- Esqueci senha em 3 etapas (request, verify-code, confirm com `challengeId`).
- Desbloqueio de conta no fluxo de recuperacao de senha.
- Controle de tentativas falhas com bloqueio temporario exponencial e bloqueio persistente.
- Historico das ultimas 5 senhas para bloquear reaproveitamento.
- CRUD basico de `Sistema`, `Role`, `Permissao` e vinculo `UsuarioSistema` com paginacao.
- Endpoint de health para validar JWT.
- Job agendado para limpar refresh tokens expirados.

TODOs futuros (vindos de `notas.md` e ainda nao implementados no codigo):

- Sessao por device (IP/dispositivo/localizacao) para contexto de risco.
- Rate limit para login/esqueci senha.
- Invalidacao de todas as sessoes apos troca de senha em todos os fluxos (parcial hoje).
- Modelo de paginacao customizado.
- Melhor padronizacao de erros.

## 4. Fluxos de Autenticacao

### 4.1 Login -> Access + Refresh
1. Cliente envia `POST /auth/login` com email, senha e sistema.
2. API valida credenciais e estado de bloqueio.
3. API envia codigo de 6 digitos por email (`TipoVerificacao.LOGIN`).
4. Cliente confirma em `POST /auth/login/verify-code`.
5. API valida codigo e retorna tokens.

Fluxo textual:

```text
POST /auth/login
  -> valida senha + tentativas + vinculo usuario-sistema
  -> gera codigo por email
POST /auth/login/verify-code
  -> valida codigo
  -> gera access JWT
  -> cria refresh rotativo (tokenId:tokenRaw)
```

### 4.2 Rotacao de Refresh Token
1. Cliente envia `POST /auth/refresh-token` com refresh atual.
2. API separa `tokenId:raw`, busca por `tokenId`, compara hash e valida status.
3. Se valido: marca antigo como `used/revoked`, cria novo refresh e novo access.
4. Se invalido/reutilizado/adulterado: revoga todas as sessoes do usuario.

```text
refresh invalido/reuso/adulterado -> revokeAllByUsuario -> erro 400
refresh valido -> rotate -> novo access + novo refresh
```

### 4.3 Logout
- `POST /auth/logout` (autenticado) revoga todos os refresh tokens do usuario autenticado.

### 4.4 Reset de Senha (Esqueci Senha)
1. `POST /auth/esqueci-senha/request`: sempre retorna sucesso (evita enumeracao de email).
2. `POST /auth/esqueci-senha/verify-code`: valida codigo e devolve `challengeId` temporario.
3. `POST /auth/esqueci-senha/confirm`: valida `challengeId`, troca senha, revoga sessoes antigas e devolve novos tokens.

### 4.5 Bloqueio de Conta por Falha de Login
- A partir da 3a tentativa falha, aplica bloqueio temporario exponencial (5, 10, 20, 40, 80 minutos...).
- Em tentativas altas (implementacao atual considera `>= 7`), usuario pode ficar bloqueado ate trocar senha.

### 4.6 Reuse de Refresh
- Se token reutilizado ou adulterado for detectado, a API executa revogacao global dos refresh tokens do usuario por seguranca.

## 5. Endpoints

<img width="1231" height="598" alt="image" src="https://github.com/user-attachments/assets/98f80779-d5b2-451b-abc4-8e624820a63a" />


> Base path local: `http://localhost:8080`

### 5.1 Auth

#### POST `/auth/register`
Cria usuario.

Request:
```json
{
  "nome": "Maria Silva",
  "email": "maria@email.com",
  "senha": "Senha@123"
}
```

Response `201`:
```json
{
  "id": "uuid",
  "nome": "Maria Silva",
  "email": "maria@email.com",
  "emailConfirmado": false,
  "createdAt": "2026-04-03T12:00:00",
  "updatedAt": "2026-04-03T12:00:00"
}
```

---

#### POST `/auth/login`
Valida credenciais e envia codigo por email.

Request:
```json
{
  "email": "maria@email.com",
  "senha": "Senha@123",
  "sistemaId": "uuid"
}
```

Response `204 No Content`.

---

#### POST `/auth/login/verify-code`
Valida codigo de login e emite tokens.

Request:
```json
{
  "code": "123456"
}
```

Response `200`:
```json
{
  "accessToken": "jwt",
  "refreshToken": "tokenId:tokenRaw"
}
```

---

#### POST `/auth/refresh-token`
Rotaciona refresh e gera novo access token.

Request:
```json
{
  "refreshToken": "tokenId:tokenRaw"
}
```

Response `200`:
```json
{
  "accessToken": "jwt",
  "refreshToken": "novoTokenId:novoTokenRaw"
}
```

Observacoes:
- Reuso/adulteracao pode encerrar todas as sessoes do usuario.

---

#### PUT `/auth/alterar-senha` *(autenticado via Bearer token)*
Inicia alteracao de senha (envia codigo por email).

Request:
```json
{
  "senhaAtual": "Senha@123",
  "novaSenha": "NovaSenha@123"
}
```

Response `204 No Content`.

---

#### POST `/auth/alterar-senha/verify-code`
Confirma alteracao de senha com codigo.

Request:
```json
{
  "code": "123456"
}
```

Response `204 No Content`.

---

#### POST `/auth/logout` *(autenticado via Bearer token)*
Revoga todos os refresh tokens do usuario logado.

Response `204 No Content`.

---

#### POST `/auth/esqueci-senha/request`
Solicita codigo de recuperacao.

Request:
```json
{
  "email": "maria@email.com"
}
```

Response `204 No Content`.

---

#### POST `/auth/esqueci-senha/verify-code`
Valida codigo de recuperacao e retorna `challengeId`.

Request:
```json
{
  "email": "maria@email.com",
  "code": "123456"
}
```

Response `200`:
```json
{
  "challengeId": "uuid"
}
```

---

#### POST `/auth/esqueci-senha/confirm`
Finaliza recuperacao de senha e emite novos tokens.

Request:
```json
{
  "challengeId": "uuid",
  "novaSenha": "NovaSenha@123",
  "confirmarNovaSenha": "NovaSenha@123",
  "sistemaId": "uuid"
}
```

Response `200`:
```json
{
  "accessToken": "jwt",
  "refreshToken": "tokenId:tokenRaw"
}
```

### 5.2 Health

#### GET `/health/validateJWT` *(autenticado via Bearer token)*
Response `200`:
```json
{
  "message": "JWT funcionando"
}
```

### 5.3 Sistema

#### POST `/sistema/criar`
Cria sistema.

Payload esperado (binding atual sem `@RequestBody`):
```json
{
  "nome": "Portal RH",
  "descricao": "Sistema de RH"
}
```

#### GET `/sistema/listar?page=0&size=10`
Lista paginada.

#### GET `/sistema/buscar/{id}`
Busca por id (implementacao atual usa `@RequestParam UUID id`, apesar da rota conter `{id}`).

### 5.4 Role

#### POST `/role/criar`
Cria role.

Payload esperado:
```json
{
  "sistemaId": "uuid",
  "nome": "ADMIN",
  "descricao": "Administrador"
}
```

#### GET `/role/listar?page=0&size=10`
Lista paginada.

#### GET `/role/buscar/{id}`
Busca por id (implementacao atual usa `@RequestParam UUID id`).

### 5.5 Permissao

#### POST `/permissao/criar`
Cria permissao.

Request:
```json
{
  "nome": "USUARIO_EDITAR",
  "descricao": "Editar usuario",
  "roleId": "uuid"
}
```

#### GET `/permissao/listar?page=0&size=10`
Lista paginada.

#### PUT `/permissao/editar/{id}`
Edita permissao.

### 5.6 UsuarioSistema

#### POST `/usuarioSistema`
Vincula usuario a sistema e role.

Payload esperado:
```json
{
  "usuarioId": "uuid",
  "sistemaId": "uuid",
  "roleId": "uuid"
}
```

#### GET `/usuarioSistema?usuarioId=&sistemaId=&page=0&size=10`
Lista com filtros opcionais.

#### GET `/usuarioSistema/{id}`
Busca vinculo por id.

#### PATCH `/usuarioSistema/{id}/role?roleId={uuid}`
Troca role do vinculo.

#### DELETE `/usuarioSistema/{id}`
Remove vinculo.

## 6. DTOs e Estruturas

Principais DTOs de autenticacao:

- `RegistrarDTO` / `RegistrarResponseDTO`
- `LoginDTO` / `LoginResponseDTO`
- `VerifyCodeDTO`
- `RefreshTokenDTO` / `RefreshTokenResponseDTO`
- `AlterarSenhaDTO`
- `EsqueciSenhaDTO`
- `EsqueciSenhaVerifyCodeDTO` / `EsqueciSenhaVerifyResponseDTO`
- `EsqueciSenhaConfirmDTO`

DTO de erro padrao:

```json
{
  "status": 400,
  "message": "Erro de validacao",
  "details": [
    "Deve conter pelo menos 1 numero."
  ],
  "timestamp": "2026-04-03T12:00:00"
}
```

DTOs de dominio (cadastros auxiliares):

- Sistema: `CriarSistemaDTO`, `SistemaListDTO`, `SistemaDTO`
- Role: `CriarRoleDTO`, `RoleListDTO`, `RoleDTO`, `RoleResumoDTO`
- Permissao: `CriarPermissaoDTO`, `PermissaoDTO`, `PermissaoResumoDTO`
- UsuarioSistema: `CriarUsuarioSistemaDTO`, `UsuarioSistemaDTO`

## 7. Tecnologias e Dependencias

- Java 17
- Spring Boot 3.5.10
- Spring Web
- Spring Security
- Spring Data JPA
- Spring Validation
- Spring Mail
- Springdoc OpenAPI (`/swagger`)
- PostgreSQL
- H2 (runtime)
- JJWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- MapStruct
- Lombok
- Dotenv Java
- Maven Wrapper (`mvnw`, `mvnw.cmd`)

## 8. Configuracao e Execucao

### 8.1 Pre-requisitos
- Java 17+
- Docker (opcional, para Postgres)
- Maven (ou usar wrapper)

### 8.2 Variaveis de ambiente
O app carrega variaveis do arquivo `.env` no startup (`AuthApplication`).

Variaveis necessarias:

- `JWT_SECRET`
- `JWT_EXPIRATION`
- `JWT_REFRESH_EXPIRATION`
- `VERIFICATION_CODE_EXPIRATION`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

> Recomendacao: use credenciais de email de app password e **nao versionar secrets reais**.

### 8.3 Banco
`docker-compose.yml` sobe PostgreSQL 16 em `localhost:5433`, DB `authdb`.

### 8.4 Executar localmente

```powershell
cd D:\repositorio_github\AuthAPI

docker compose up -d

.\mvnw.cmd spring-boot:run
```

Swagger UI:
- `http://localhost:8080/swagger`

### 8.5 Testes
Atualmente existe teste basico de contexto (`AuthApplicationTests`).

```powershell
cd D:\repositorio_github\AuthAPI

.\mvnw.cmd test
```

## 9. TODO / Proximos Passos

- Sessao por device e trilha de login suspeito.
- Rate limit por IP para endpoints sensiveis.
- Melhorar consistencia de erros e contratos HTTP.
- Revisar revogacao total de sessao apos troca de senha em todos os cenarios.
- Ampliar testes automatizados (unitarios e integracao para fluxos de auth).
- Revisar endpoints com inconsistencias de binding (`@PathVariable` vs `@RequestParam`, ausencia de `@RequestBody` em alguns POSTs).

## 10. Historico de Desenvolvimento
Resumo consolidado de `notas.md`:

- **28/03/26**
  - Lombok e refatoracao de DTOs/entities.
  - Paginacao nos GETs.
  - Primeiro endpoint de login com access token.

- **29/03/26**
  - Refresh token (7 dias) + endpoint de renovacao.
  - Padronizacao inicial de erros com exceptions e middleware global.

- **30/03/26**
  - Investigacao de problema de senha ficando `null`.

- **01/04/26**
  - Adicao de logs para troubleshooting.
  - Endpoint de logout invalidando refresh token.

- **02/04/26**
  - Deteccao de reuse de refresh e revogacao de tokens ativos.
  - Bloqueio progressivo apos tentativas falhas de login.

- **03/04/26**
  - Fluxo completo de esqueci senha (request/verify/confirm).
  - Persistencia de refresh com hash + id publico (`tokenId`).
  - Observacao de melhoria pendente para robustez de revogacao pos-reuse.

## 11. Observacoes

Boas praticas aplicadas:

- Refresh token armazenado com hash (nao em texto puro).
- Rotacao de refresh e resposta de seguranca para reuso.
- Mascaramento de email em logs (`LogSanitizer`).
- Correlacao de request com `X-Request-Id` via MDC.
- `@RestControllerAdvice` com formato padrao de erro.
- Validacao de senha forte e historico de senhas.

Como contribuir:

1. Crie uma branch de feature/fix.
2. Adicione testes para comportamento alterado.
3. Padronize contratos HTTP/DTO e mensagens de erro.
4. Abra PR com descricao objetiva dos cenarios cobertos.

