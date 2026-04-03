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
>- [ ] Guardar sessão do usuário por device (ip, pc/celular, localização resumida) — usar para lógica de login suspeito
>- [x] Verificar se refresh inválido foi usado para tentar acessar o sistema (limpar tokens ativos)
>- [ ] Criar um model de paginação personalizado pra substituir o padrão do spring
>- [ ] Invalidar sessões depois de trocar senha (no fluxo de "alterar senha" e "esqueci senha")
>- [ ] rate limit por ip para login, esqueci senha, etc
>- [ ] Melhorar padronização de erros

---
## Separação por contexto

### 🔐 Segurança
- [x] refresh token (7 dias)
- [x] validação de email com código
- [x] bloqueio por tentativas de login
- [x] detectar uso de refresh inválido (revogar sessões)
- [ ] invalidar sessão dps de mudar senha
- [ ] rate limit por ip para login, esqueci senha, etc

### 🔑 Senha
- [x] padrão forte
- [x] não reutilizar últimas senhas
- [x] fluxo de mudança de senha

### 👤 Usuário
- [x] DTO de retorno
- [x] email unique

### 🚪 Sessão
- [x] logout (invalidar refresh token)
- [ ] sessão por device (ip, pc/celular, localização)

### ⚠️ Erros & Infra
- [x] exceptions + middlewares
- [ ] melhorar padronização

---

## ⚠️ Problemas Atuais

    - nada agr


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

    - arruma fluxo de reuso do refresh token

    