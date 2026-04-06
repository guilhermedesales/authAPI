package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Auth.AlterarSenhaDTO;
import com.api.auth.Application.DTOs.Auth.EsqueciSenha.EsqueciSenhaConfirmDTO;
import com.api.auth.Application.DTOs.Auth.EsqueciSenha.EsqueciSenhaDTO;
import com.api.auth.Application.DTOs.Auth.EsqueciSenha.EsqueciSenhaVerifyCodeDTO;
import com.api.auth.Application.DTOs.Auth.EsqueciSenha.EsqueciSenhaVerifyResponseDTO;
import com.api.auth.Application.DTOs.Auth.Login.LoginDTO;
import com.api.auth.Application.DTOs.Auth.Login.LoginResponseDTO;
import com.api.auth.Application.DTOs.Auth.OtpChallengeResponseDTO;
import com.api.auth.Application.DTOs.Auth.RefreshToken.RefreshTokenDTO;
import com.api.auth.Application.DTOs.Auth.RefreshToken.RefreshTokenResponseDTO;
import com.api.auth.Application.DTOs.Auth.Registrar.RegistrarDTO;
import com.api.auth.Application.DTOs.Auth.Registrar.RegistrarResponseDTO;
import com.api.auth.Application.DTOs.Auth.VerifyCodeDTO;
import com.api.auth.Application.DTOs.Auth.RequestContext;
import com.api.auth.Application.Exceptions.NotFoundException;
import com.api.auth.Application.Service.AuthService;
import com.api.auth.Application.Service.JwtService;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Application.Utils.LogSanitizer;
import com.api.auth.API.Config.ClientIpResolver;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.UsuarioSistema;
import com.api.auth.Infra.Repositories.UsuarioSistemaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Endpoints de autenticação, sessão e recuperação de acesso")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UsuarioSistemaRepository usuarioSistemaRepository;
    private final ClientIpResolver clientIpResolver;

    public AuthController(AuthService authService,
                          JwtService jwtService,
                          UsuarioSistemaRepository usuarioSistemaRepository,
                          ClientIpResolver clientIpResolver) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.usuarioSistemaRepository = usuarioSistemaRepository;
        this.clientIpResolver = clientIpResolver;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registrar usuário",
            description = "Cria uma nova conta de usuário com validação de email e política de senha."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<RegistrarResponseDTO> registrar(@Valid @RequestBody RegistrarDTO dto) {
        log.info("[AUTH] Registro iniciado - email={}", LogSanitizer.maskEmail(dto.getEmail()));
        RegistrarResponseDTO response = authService.registrar(dto);
        log.info("[AUTH] Registro concluido - usuarioId={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar login",
            description = "Valida credenciais do usuário e envia um código OTP por email. Fluxo: login -> envio de código -> verificação -> geração de tokens."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Challenge OTP gerado"),
            @ApiResponse(responseCode = "400", description = "Credenciais inválidas ou usuário bloqueado"),
            @ApiResponse(responseCode = "429", description = "Limite de tentativas excedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<OtpChallengeResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        log.info("[AUTH] Login iniciado - email={} sistemaId={}", LogSanitizer.maskEmail(dto.getEmail()), dto.getSistemaId());
        UUID challengeId = authService.login(dto);
        log.info("[AUTH] Login validado - challenge enviado - email={}", LogSanitizer.maskEmail(dto.getEmail()));
        return ResponseEntity.ok(new OtpChallengeResponseDTO(challengeId));
    }

    @PostMapping("/login/verify-code")
    @Operation(
            summary = "Verificar OTP do login",
            description = "Valida o código OTP do challenge de login. Emite access/refresh token quando risco é baixo/médio ou retorna step-up quando risco é alto."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login concluído ou step-up requerido"),
            @ApiResponse(responseCode = "400", description = "Código inválido/expirado ou challenge inválido"),
            @ApiResponse(responseCode = "429", description = "Limite de tentativas excedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<LoginResponseDTO> verificarCodigoLogin(
            @RequestBody @Valid VerifyCodeDTO dto,
            HttpServletRequest request
    ) {
        RequestContext contexto = buildRequestContext(request);
        contexto.setDeviceId(dto.getDeviceId());
        return ResponseEntity.ok(authService.verifyCodeLogin(dto, contexto));
    }

    @PostMapping("/login/step-up/verify-code")
    @Operation(
            summary = "Verificar OTP de step-up",
            description = "Conclui a autenticação adicional para login de alto risco. Use este endpoint quando o retorno de /auth/login/verify-code vier com stepUpRequired=true."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login concluído com autenticação adicional"),
            @ApiResponse(responseCode = "400", description = "Código inválido/expirado"),
            @ApiResponse(responseCode = "429", description = "Limite de tentativas excedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<LoginResponseDTO> verificarCodigoLoginStepUp(
            @RequestBody @Valid VerifyCodeDTO dto,
            HttpServletRequest request
    ) {
        RequestContext contexto = buildRequestContext(request);
        contexto.setDeviceId(dto.getDeviceId());
        return ResponseEntity.ok(authService.verifyStepUpLogin(dto, contexto));
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Atualizar tokens",
            description = "Rotaciona o refresh token e emite novo access token. Use quando o access token expirar."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens atualizados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido, expirado ou reutilizado"),
            @ApiResponse(responseCode = "429", description = "Limite de tentativas excedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<RefreshTokenResponseDTO> atualizarToken(@Valid @RequestBody RefreshTokenDTO dto) {
        log.info("[AUTH] Atualizacao de token iniciada");
        String tokenSolicitado = dto.getRefreshToken();

        // Service performs reuse detection + rotation in a single transaction.
        JwtService.RefreshRotationResult resultadoRotacao = jwtService.rotateRefreshToken(tokenSolicitado);
        Usuario usuario = resultadoRotacao.usuario();
        if (resultadoRotacao.session().getSistema() == null) {
            throw new NotFoundException(ErrorMessages.Recursos.SISTEMA_NAO_ENCONTRADO);
        }

        UsuarioSistema vinculoUsuarioSistema = usuarioSistemaRepository
                .findByUsuarioAndSistema(usuario, resultadoRotacao.session().getSistema())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.Recursos.USUARIO_NAO_ENCONTRADO_SISTEMA));

        String accessToken = jwtService.generateToken(vinculoUsuarioSistema, resultadoRotacao.session());
        log.info("[AUTH] Atualizacao de token concluida - usuarioId={}", usuario.getId());
        return ResponseEntity.ok(new RefreshTokenResponseDTO(accessToken, resultadoRotacao.refreshToken()));
    }

    @PutMapping("/alterar-senha")
    @Operation(
            summary = "Solicitar alteração de senha",
            description = "Valida senha atual e envia OTP para confirmação da troca de senha."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Challenge de validação enviado"),
            @ApiResponse(responseCode = "400", description = "Senha atual inválida ou nova senha fora da política"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<OtpChallengeResponseDTO> alterarSenha(@RequestBody @Valid AlterarSenhaDTO dto,
                                                                @Parameter(description = "ID do usuário autenticado extraído do token JWT", hidden = true)
                                                                @AuthenticationPrincipal String usuarioId) {
        log.info("[AUTH] Alteracao de senha iniciada - usuarioId={}", usuarioId);
        UUID challengeId = authService.alterarSenha(UUID.fromString(usuarioId), dto);
        log.info("[AUTH] Alteracao de senha - challenge enviado - usuarioId={}", usuarioId);
        return ResponseEntity.ok(new OtpChallengeResponseDTO(challengeId));
    }

    @PostMapping("/alterar-senha/verify-code")
    @Operation(
            summary = "Confirmar alteração de senha",
            description = "Valida o OTP da troca de senha e aplica a nova senha de forma definitiva."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Código inválido/expirado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> verificarCodigoAlteracaoSenha(@RequestBody @Valid VerifyCodeDTO dto) {
        log.info("[AUTH] Verificacao de codigo para alteracao de senha iniciada - tamanhoCodigo={}", dto.getCode() == null ? 0 : dto.getCode().length());
        authService.confirmarAlteracaoSenha(dto.getChallengeId(), dto.getCode());
        log.info("[AUTH] Alteracao de senha concluida via codigo");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Realizar logout",
            description = "Revoga a sessão atual (ou todas as sessões em fallback legado) e invalida tokens vinculados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout concluído"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        authService.logout(authorizationHeader);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/esqueci-senha/request")
    @Operation(
            summary = "Solicitar recuperação de senha",
            description = "Inicia o fluxo de recuperação de senha enviando OTP para o email informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Solicitação processada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "429", description = "Limite de tentativas excedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<Void> solicitarRecuperacaoSenha(@RequestBody @Valid EsqueciSenhaDTO dto) {
        log.info("[AUTH] Recuperacao de senha solicitada - email={}", LogSanitizer.maskEmail(dto.getEmail()));
        authService.esqueciSenha(dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/esqueci-senha/verify-code")
    @Operation(
            summary = "Validar OTP de recuperação",
            description = "Valida o código OTP de recuperação e retorna challenge para confirmar a nova senha."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Código validado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Código inválido/expirado"),
            @ApiResponse(responseCode = "429", description = "Limite de tentativas excedido"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<EsqueciSenhaVerifyResponseDTO> verificarCodigoRecuperacaoSenha(@RequestBody @Valid EsqueciSenhaVerifyCodeDTO dto) {
        UUID challengeId = authService.verificarCodigoEsqueciSenha(dto);
        return ResponseEntity.ok(new EsqueciSenhaVerifyResponseDTO(challengeId));
    }

    @PostMapping("/esqueci-senha/confirm")
    @Operation(
            summary = "Confirmar recuperação de senha",
            description = "Finaliza a recuperação de senha usando challenge válido e já devolve novos tokens de acesso."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha redefinida e sessão iniciada"),
            @ApiResponse(responseCode = "400", description = "Challenge inválido/expirado ou senha fora da política"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    public ResponseEntity<LoginResponseDTO> confirmarRecuperacaoSenha(@RequestBody @Valid EsqueciSenhaConfirmDTO dto, HttpServletRequest request) {
        LoginResponseDTO response = authService.confirmarEsqueciSenha(dto, buildRequestContext(request));
        return ResponseEntity.ok(response);
    }

    private RequestContext buildRequestContext(HttpServletRequest request) {
        RequestContext context = new RequestContext();
        context.setIp(clientIpResolver.resolve(request));
        context.setUserAgent(request.getHeader("User-Agent"));
        return context;
    }
}
