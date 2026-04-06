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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;

    @Value("${VERIFICATION_CODE_EXPIRATION}")
    private long expirationMs;

    @Value("${VERIFICATION_CHALLENGE_EXPIRATION:600000}")
    private long challengeExpirationMs;

    @Value("${auth.otp.max-attempts:5}")
    private int otpMaxAttempts;

    @Transactional
    public void generateAndSend(Usuario usuario, TipoVerificacao tipo) {
        generateAndSend(usuario, tipo, null);
    }

    @Transactional
    public void generateAndSend(Usuario usuario, TipoVerificacao tipo, String novaSenhaHash) {
        log.info("[AUTH] Generating verification code - userId={} tipo={}", usuario.getId(), tipo);
        verificationCodeRepository.deleteByUsuarioAndTipo(usuario, tipo);
        verificationCodeRepository.flush();

        String code = String.format("%06d", new SecureRandom().nextInt(999999));

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setUsuario(usuario);
        verificationCode.setCode(code);
        verificationCode.setExpiryDate(Instant.now().plusMillis(expirationMs));
        verificationCode.setUsed(false);
        verificationCode.setTipo(tipo);
        verificationCode.setNovaSenhaHash(novaSenhaHash);
        verificationCode.setChallengeId(null);
        verificationCode.setChallengeExpiryDate(null);
        verificationCode.setChallengeUsed(false);
        verificationCode.setSistemaId(null);
        verificationCode.setAttempts(0);

        verificationCodeRepository.save(verificationCode);
        emailService.sendVerificationCode(usuario.getEmail(), code);
        log.info("[AUTH] Verification code generated and sent - userId={} email={} tipo={}",
                usuario.getId(), LogSanitizer.maskEmail(usuario.getEmail()), tipo);
    }

    @Transactional
    public UUID generateAndSendChallenge(Usuario usuario, TipoVerificacao tipo, String novaSenhaHash, UUID sistemaId) {
        log.info("[AUTH] Generating challenge-based verification code - userId={} tipo={} sistemaId={}",
                usuario.getId(), tipo, sistemaId);
        verificationCodeRepository.deleteByUsuarioAndTipo(usuario, tipo);
        verificationCodeRepository.flush();

        String code = String.format("%06d", new SecureRandom().nextInt(999999));
        UUID challengeId = UUID.randomUUID();

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setUsuario(usuario);
        verificationCode.setCode(code);
        verificationCode.setExpiryDate(Instant.now().plusMillis(expirationMs));
        verificationCode.setUsed(false);
        verificationCode.setTipo(tipo);
        verificationCode.setNovaSenhaHash(novaSenhaHash);
        verificationCode.setChallengeId(challengeId);
        verificationCode.setChallengeExpiryDate(Instant.now().plusMillis(challengeExpirationMs));
        verificationCode.setChallengeUsed(false);
        verificationCode.setSistemaId(sistemaId);
        verificationCode.setAttempts(0);

        verificationCodeRepository.save(verificationCode);
        emailService.sendVerificationCode(usuario.getEmail(), code);
        log.info("[AUTH] Challenge verification code generated and sent - userId={} challengeId={} tipo={}",
                usuario.getId(), challengeId, tipo);
        return challengeId;
    }

    @Transactional
    public VerificationCode validateCodeByChallenge(UUID challengeId, String code, TipoVerificacao tipoEsperado) {
        log.info("[AUTH] Validating challenge-based verification code - tipo={} challengeId={}",
                tipoEsperado, challengeId);

        VerificationCode verificationCode = verificationCodeRepository
                .findByChallengeIdAndTipo(challengeId, tipoEsperado)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.CodigoEmail.CODIGO_INVALIDO));

        if (verificationCode.isUsed() || verificationCode.isChallengeUsed()) {
            throw new ValidationException(ErrorMessages.CodigoEmail.CODIGO_UTILIZADO);
        }

        if (verificationCode.getExpiryDate().isBefore(Instant.now()) ||
                verificationCode.getChallengeExpiryDate() == null ||
                verificationCode.getChallengeExpiryDate().isBefore(Instant.now())) {
            throw new ValidationException(ErrorMessages.CodigoEmail.CODIGO_EXPIRADO);
        }

        if (!verificationCode.getCode().equals(code)) {
            registerFailedAttempt(verificationCode, true);
            throw new ValidationException(ErrorMessages.CodigoEmail.CODIGO_INVALIDO);
        }

        verificationCode.setUsed(true);
        verificationCode.setChallengeUsed(true);
        verificationCodeRepository.save(verificationCode);

        Usuario usuario = verificationCode.getUsuario();
        if (!usuario.isEmailConfirmado()) {
            usuario.setEmailConfirmado(true);
            usuarioRepository.save(usuario);
            log.info("[AUTH] Email confirmed - userId={}", usuario.getId());
        }

        return verificationCode;
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

    @Transactional
    public UUID validateForgotPasswordCode(String email, String code) {
        String maskedEmail = LogSanitizer.maskEmail(email);
        log.info("[AUTH] Validating forgot-password code - email={}", maskedEmail);

        VerificationCode verificationCode = verificationCodeRepository
                .findTopByUsuarioEmailAndTipoAndUsedFalseOrderByExpiryDateDesc(email, TipoVerificacao.ESQUECI_SENHA)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.CodigoEmail.CODIGO_INVALIDO));

        if (verificationCode.isUsed()) {
            throw new ValidationException(ErrorMessages.CodigoEmail.CODIGO_UTILIZADO);
        }

        if (verificationCode.getExpiryDate().isBefore(Instant.now())) {
            throw new ValidationException(ErrorMessages.CodigoEmail.CODIGO_EXPIRADO);
        }

        if (!verificationCode.getCode().equals(code)) {
            registerFailedAttempt(verificationCode, false);
            throw new ValidationException(ErrorMessages.CodigoEmail.CODIGO_INVALIDO);
        }

        verificationCode.setUsed(true);
        verificationCode.setChallengeId(UUID.randomUUID());
        verificationCode.setChallengeExpiryDate(Instant.now().plusMillis(challengeExpirationMs));
        verificationCode.setChallengeUsed(false);
        verificationCodeRepository.save(verificationCode);

        log.info("[AUTH] Forgot-password code validated - userId={} challengeId={}",
                verificationCode.getUsuario().getId(), verificationCode.getChallengeId());
        return verificationCode.getChallengeId();
    }

    @Transactional
    public VerificationCode validateForgotPasswordChallenge(UUID challengeId) {
        VerificationCode verificationCode = verificationCodeRepository
                .findByChallengeIdAndTipo(challengeId, TipoVerificacao.ESQUECI_SENHA)
                .orElseThrow(() -> new ValidationException(ErrorMessages.CodigoEmail.CODIGO_INVALIDO));

        if (!verificationCode.isUsed()) {
            throw new ValidationException(ErrorMessages.CodigoEmail.CODIGO_INVALIDO);
        }

        if (verificationCode.isChallengeUsed()) {
            throw new ValidationException(ErrorMessages.CodigoEmail.CODIGO_UTILIZADO);
        }

        if (verificationCode.getChallengeExpiryDate() == null ||
                verificationCode.getChallengeExpiryDate().isBefore(Instant.now())) {
            throw new ValidationException(ErrorMessages.CodigoEmail.CODIGO_EXPIRADO);
        }

        verificationCode.setChallengeUsed(true);
        verificationCodeRepository.save(verificationCode);
        return verificationCode;
    }

    private void registerFailedAttempt(VerificationCode verificationCode, boolean challengeFlow) {
        int currentAttempts = verificationCode.getAttempts() == null ? 0 : verificationCode.getAttempts();
        int attempts = currentAttempts + 1;
        verificationCode.setAttempts(attempts);

        if (attempts >= otpMaxAttempts) {
            verificationCode.setUsed(true);
            if (challengeFlow) {
                verificationCode.setChallengeUsed(true);
            }
            verificationCodeRepository.save(verificationCode);
            throw new ValidationException(ErrorMessages.CodigoEmail.CODIGO_TENTATIVAS_EXCEDIDAS);
        }

        verificationCodeRepository.save(verificationCode);
    }
}
