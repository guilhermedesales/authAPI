# TODO LIST


>## 🧠 BrainStorm de funcionalidades
>
>- [x] Criar um DTO de retorno de usuário
>- [x] Adicionar refresh token
>- [x] Adicionar validação do email com código enviado para o email registrado
>- [x] Adicionar bloqueio de usuário com muitas tentativas de login
>- [x] Adicionar fluxo de mudança de senha
>- [x] Adicionar mensagens de erro padronizadas / exceptions e middlewares (ainda precisa melhorar)
>- [x] Senha com padrão forte (1 letra maiúscula, 1 letra minúscula, 1 caracter especial, 1 número, mínimo 8 caracteres)
>- [x] Email deve ser único
>- [x] Não permitir mudar senha para uma senha já usada antes (últimas 3–5)
>- [x] Endpoint de logout (invalida refresh token)
>- [x] Guardar sessão do usuário por device (ip, pc/celular, localização resumida) — usar para lógica de login suspeito
>- [x] Verificar se refresh inválido foi usado para tentar acessar o sistema (limpar tokens ativos)
>- [ ] Criar um model de paginação personalizado pra substituir o padrão do spring
>- [x] Invalidar sessões depois de trocar senha (no fluxo de "alterar senha" e "esqueci senha")
>- [x] rate limit por ip para login, esqueci senha, etc
>- [ ] Melhorar padronização de erros
>- [x] Add get sessão por user com filtros de busca por location, device
>- [x] Implementar autorização por role/permissão (RBAC)
>- [x] Adicionar roles/permissões no JWT (authorities)
>- [x] Proteger endpoints administrativos (sistema, role, permissao, usuarioSistema)
>- [ ] Evitar enumeração de usuário no login (resposta padrão)
>- [ ] Implementar revogação de access token (blacklist / Redis)
>- [ ] Resolver race condition no refresh token
>- [x] Hash do OTP (não salvar código em texto puro)
>- [ ] Melhorar rate limit (não só IP, incluir email/user/device)
>- [ ] Limitar quantidade de sessões por usuário
>- [ ] Criar auditoria de segurança (login, refresh, senha, logout)
>- [ ] Criar testes de integração (login, refresh, esqueci senha)
>- [ ] Adicionar @Version nas entidades críticas
>- [ ] Melhorar modelagem (índices e constraints)
>- [ ] Criar limpeza automática (OTP, refresh, sessões)
>- [ ] Melhorar logs pensando no front
>- [ ] Implementar 2FA (Google Authenticator / TOTP)
>- [ ] Login com Google (OAuth2)
>- [ ] Perguntar se quer manter sessões ativas no fluxo de mudar senha

---

## Separação por contexto

### 🔐 Segurança
- [x] refresh token (7 dias)
- [x] validação de email com código
- [x] bloqueio por tentativas de login
- [x] detectar uso de refresh inválido (revogar sessões)
- [ ] rate limit por ip para login, esqueci senha, etc
- [x] autorização por role/permissão (RBAC)
- [x] roles/permissões no JWT (authorities)
- [x] proteção de endpoints administrativos
- [ ] evitar enumeração de usuário no login
- [ ] revogação de access token
- [x] hash de OTP
- [ ] auditoria de segurança

### 🔑 Senha
- [x] padrão forte
- [x] não reutilizar últimas senhas
- [x] fluxo de mudança de senha

### 👤 Usuário
- [x] DTO de retorno
- [x] email unique

### 🚪 Sessão
- [x] logout (invalidar refresh token)
- [x] sessão por device (ip, pc/celular, localização)
- [x] get sessão por user com filtros de busca por location, device
- [ ] invalidar sessões ativas depois de trocar senha (no fluxo de "alterar senha" e "esqueci senha")
- [x] logout invalida apenas a sessão atual
- [ ] limitar sessões por usuário

### ⚠️ Erros & Infra
- [x] exceptions + middlewares
- [ ] melhorar padronização
- [ ] testes de integração

---

## ⚠️ Problemas Atuais

    - Race condition no refresh token
    - Falta revogação de access token
    - Enumeração de usuário no login
    - Rate limit apenas por IP (fraco contra botnet)

---


# HISTÓRICO DE DESENVOLVIMENTO 


## 28/03/26

    - add lombok e refatora os dtos e entities

    - add paginação nos gets (paginação padrão do spring, talvez criar um model de paginação personalizada)

    - add endpoint de login q retrorna o access token (melhorar isso com refresh e validação por email)

## 29/03/26

    - add refresh token e endpoint pra gerar um novo access token usando o refresh token (refresh de 7 dias)

    - padroniza mensagens de erro e add exceptions e middlewares

## 30/03/26

    fluxo de mudar senha ta falhando, a senha na tabela do user fica null

    adicionar logs pra encontrar problema

## 01/04/26

    - adicinei os logs e no fim não aconteceu mais o problema da senha null

    - adicionei endpoint de logout q invalida o refresh token (talvez adicionar uma blacklist pra invalidar o access token tbm)

## 02/04/26

    - add verificação de reuso do refresh token e invalida tokens ativos do user (melhorar isso com seções por device)

    - add bloqueio de user apos errar a senha no login (3° tentativa bloq por 5 min as proximas em diante dobra, na 8° tentativa bloqueia o user ate mudar a senha)

## 03/04/26

    - add fluxo de esqueci senha com 3 endpoints (enviar cod para email, validar cod e mudar senha com challengeId q retorna da verificação do cod)

    - salva apenas hash do refresh no banco e cria id publico pra busca

    - arrumei fluxo de reuso do refresh token

## 04/04/26

    - add sessão por device (guarda ip, location e tipo de device) - usa o ip-api por enquanto

    - add device id para identificar se é o mesmo device ou não

    - logout por sessão (invalida apenas a sessão atual)

    - add get device por user

    - add rate limit por ip para (login: 10 req / 60s, verify-code: 6 req / 600s, forgot-password request: 5 req / 900s, forgot-password verify: 8 req / 600s, refresh: 30 req / 60s

## 06/04/26

    - add challengeId em todos os fluxos q possuem OTP (cod de verificação do email)

    - hotfix permissao service não estava vinculando a uma role

    - rate limit com redis

    - OTP com limite de tentativas

    - retry no geolocation

    - add score de risco no login para ip, endereço e device

    - add documentação no swagger

## 09/04/26

    - implementação completa de RBAC (multi-tenant)

    - introdução de GLOBAL_ADMIN (escopo global sem sistema)

    - bootstrap automático de roles por sistema (USER, ADMIN)

    - criação de sistema default para resolver problema de bootstrap
    
    - auto-provisionamento de vínculo UsuarioSistema (register + first login)
    
    - proteção de endpoints via @PreAuthorize (Spring Method Security)

    - enforcement de escopo por sistema (RBACAuthorizationService)

    - inclusão de authorities no JWT + parsing no JwtAuthFilter

    - fallback de sistema (resolveSystemOrDefault)

    - inicialização de RBAC no startup (initializer)

    - ajustes de DTOs para suportar multi-tenant

    - testes iniciais de bootstrap (RBAC)

    - correção de encoding do projeto (UTF-8)

## 10/04/26

     - so usa o sistema default se não tiver nenhum outro sistema registrado

    - otp salvo em hash no banco

    - pega o deviceId no fluxo de esqueci senha (evita duplicar sessões de mesmo device e sistema)

## 15/04/26

    - endpoints para listar e atualizar user, a lista é baseado no sistema, um adm de um sistema não pode visualizar users de iutro sistema

    - começo da implementação de revogar sessoes opcional no fluxo de mudar senha (não registrou valor do campo revogarSessoes na tabela de verificationCode)

## 16/04/26

    - revogar sessões dps de mudar a senha no fluxo de alterar senha é opcional, mas a sessão atual vinda do token é mantida