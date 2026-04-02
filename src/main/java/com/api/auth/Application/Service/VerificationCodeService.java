package com.api.auth.Application.Service;

import com.api.auth.Application.Exceptions.NotFoundException;
import com.api.auth.Application.Exceptions.ValidationException;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Application.Utils.LogSanitizer;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.VerificationCode;
import com.api.auth.Domain.Enum.TipoVerificacao;
import com.api.auth.Infra.Repositories.UsuarioRepository;
import com.api.auth.Infra.Repositories.VerificationCodeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;

    @Value("${VERIFICATION_CODE_EXPIRATION}")
    private long expirationMs;

    @Transactional
    public void generateAndSend(Usuario usuario, TipoVerificacao tipo) {
        generateAndSend(usuario, tipo, null);
    }

    @Transactional
    public void generateAndSend(Usuario usuario, TipoVerificacao tipo, String novaSenhaHash) {
        log.info("[AUTH] Generating verification code - userId={} tipo={}", usuario.getId(), tipo);
        verificationCodeRepository.deleteByUsuario(usuario);
        verificationCodeRepository.flush();

        String code = String.format("%06d", new SecureRandom().nextInt(999999));

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setUsuario(usuario);
        verificationCode.setCode(code);
        verificationCode.setExpiryDate(Instant.now().plusMillis(expirationMs));
        verificationCode.setUsed(false);
        verificationCode.setTipo(tipo);
        verificationCode.setNovaSenhaHash(novaSenhaHash);

        verificationCodeRepository.save(verificationCode);
        emailService.sendVerificationCode(usuario.getEmail(), code);
        log.info("[AUTH] Verification code generated and sent - userId={} email={} tipo={}",
                usuario.getId(), LogSanitizer.maskEmail(usuario.getEmail()), tipo);
    }

    @Transactional
    public VerificationCode validateCode(String code, TipoVerificacao tipoEsperado) {
        log.info("[AUTH] Validating verification code - tipo={} codeLength={}",
                tipoEsperado, code == null ? 0 : code.length());
        VerificationCode verificationCode = verificationCodeRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.CodigoEmail.CODIGO_INVALIDO));

        if (verificationCode.isUsed()) {
            log.warn("[AUTH] Verification code invalid - reason=already_used userId={} tipo={}",
                    verificationCode.getUsuario().getId(), tipoEsperado);
            throw new ValidationException(ErrorMessages.CodigoEmail.CODIGO_UTILIZADO);
        }

        if (verificationCode.getExpiryDate().isBefore(Instant.now())) {
            log.warn("[AUTH] Verification code invalid - reason=expired userId={} tipo={}",
                    verificationCode.getUsuario().getId(), tipoEsperado);
            throw new ValidationException(ErrorMessages.CodigoEmail.CODIGO_EXPIRADO);
        }

        if (verificationCode.getTipo() != tipoEsperado) {
            log.warn("[AUTH] Verification code invalid - reason=wrong_type expected={} actual={} userId={}",
                    tipoEsperado, verificationCode.getTipo(), verificationCode.getUsuario().getId());
            throw new ValidationException(ErrorMessages.CodigoEmail.CODIGO_INVALIDO);
        }

        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);

        Usuario usuario = verificationCode.getUsuario();
        if (!usuario.isEmailConfirmado()) {
            usuario.setEmailConfirmado(true);
            usuarioRepository.save(usuario);
            log.info("[AUTH] Email confirmed - userId={}", usuario.getId());
        }

        log.info("[AUTH] Verification code validated - userId={} tipo={}", usuario.getId(), tipoEsperado);
        return verificationCode;
    }
}
