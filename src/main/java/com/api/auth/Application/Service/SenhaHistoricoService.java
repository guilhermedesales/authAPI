package com.api.auth.Application.Service;

import com.api.auth.Application.Exceptions.ValidationException;
import com.api.auth.Domain.Entities.SenhaHistorico;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Infra.Repositories.SenhaHistoricoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SenhaHistoricoService {

    private final SenhaHistoricoRepository senhaHistoricoRepository;
    private final PasswordEncoder encoder;

    // quantas senhas anteriores bloquear
    private static final int HISTORICO_LIMITE = 5;

    public void validarSenhaNaoReutilizada(Usuario usuario, String novaSenhaRaw) {
        log.debug("[AUTH] Checking password history - userId={} limite={}", usuario.getId(), HISTORICO_LIMITE);
        List<SenhaHistorico> historico = senhaHistoricoRepository.findUltimasSenhas(
                usuario, PageRequest.of(0, HISTORICO_LIMITE));

        boolean jaUsada = historico.stream()
                .anyMatch(h -> encoder.matches(novaSenhaRaw, h.getSenha()));

        if (jaUsada) {
            log.warn("[AUTH] Password reuse blocked - userId={} limite={}", usuario.getId(), HISTORICO_LIMITE);
            throw new ValidationException("A nova senha não pode ser igual a uma das últimas " + HISTORICO_LIMITE + " senhas utilizadas.");
        }

        log.debug("[AUTH] Password history check passed - userId={}", usuario.getId());
    }

    @Transactional
    public void salvarNoHistorico(Usuario usuario, String senhaHashAtual) {
        log.debug("[AUTH] Saving password history - userId={}", usuario.getId());
        SenhaHistorico historico = new SenhaHistorico();
        historico.setUsuario(usuario);
        historico.setSenha(senhaHashAtual);
        senhaHistoricoRepository.save(historico);
        log.info("[AUTH] Password history saved - userId={}", usuario.getId());
    }
}
