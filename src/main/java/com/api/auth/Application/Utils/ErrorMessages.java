package com.api.auth.Application.Utils;

public final class ErrorMessages {

    private ErrorMessages() {}

    public static class Auth {
        public static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos.";
        public static final String SESSAO_EXPIRADA = "Sua sessão expirou. Faça login novamente.";
        public static final String REFRESH_TOKEN_REUSE_DETECTADO = "Reutilização de refresh token detectada. Todas as sessões foram encerradas por segurança.";
        public static final String EMAIL_JA_CADASTRADO = "Esse email já possui uma conta cadastrada";
    }

    public static class Senha {
        public static final String MINIMO_CARACTERES = "Deve ter no mínimo 8 caracteres.";
        public static final String LETRA_MAIUSCULA = "Deve conter pelo menos 1 letra maiúscula.";
        public static final String LETRA_MINUSCULA = "Deve conter pelo menos 1 letra minúscula.";
        public static final String NUMERO = "Deve conter pelo menos 1 número.";
        public static final String CARACTERE_ESPECIAL = "Deve conter pelo menos 1 caractere especial.";
    }

    public static class Recursos {
        public static final String USUARIO_NAO_ENCONTRADO = "Usuário não encontrado.";
        public static final String USUARIO_NAO_ENCONTRADO_SISTEMA = "Usuário não encontrado para este sistema.";
        public static final String SISTEMA_NAO_ENCONTRADO = "Sistema não encontrado.";
        public static final String ROLE_NAO_ENCONTRADA_SISTEMA = "Role não encontrada para este sistema.";
        public static final String ROLE_NAO_ENCONTRADA = "Role não encontrada";
        public static final String PERMISSAO_NAO_ENCONTRADO = "Permissão não encontrada.";
        public static final String REFRESH_TOKEN_NAO_ENCONTRADO = "Refresh token não encontrado. Faça login novamente.";
    }

    public static class Sistema {
        public static final String ERRO_INTERNO = "Ocorreu um erro ao processar sua solicitação. Tente novamente.";
    }

    public static class CodigoEmail{
        public static final String CODIGO_INVALIDO = "Código Inválido";
        public static final String CODIGO_UTILIZADO = "Código já utilizado.";
        public static final String CODIGO_EXPIRADO = "Código expirado.";
    }

}
