package com.api.auth.Application.Service;

import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Application.Utils.LogSanitizer;
import com.api.auth.Domain.Entities.Sistema;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Infra.Repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class TentativasLoginService {

    private final UsuarioRepository usuarioRepository;

    public TentativasLoginService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String registrarTentativaFalha(Usuario usuario, Sistema sistema) {

        String maskedEmail = LogSanitizer.maskEmail(usuario.getEmail());
        usuario.setTentativasFalhas(usuario.getTentativasFalhas() + 1);

        int tentativas = usuario.getTentativasFalhas();
        LocalDateTime now = LocalDateTime.now();

        if(tentativas >=3 && tentativas <= 7){
            long tempoBloc = (long) Math.pow(2, tentativas - 3) * 5;
            usuario.setBloqueadoAte(now.plusMinutes(tempoBloc));
            log.info("[AUTH] User={} errou a senha pela {}° vez - email={} sistemaId={}", usuario.getId(), tentativas, maskedEmail, sistema.getId());
            usuarioRepository.save(usuario);
            return "Usuário bloqueado por " +tempoBloc+ " minutos devido a "+tentativas+" tentativas de login falhadas.";

        }else if(tentativas >=7){
            usuario.setBloqueado(true);
            log.warn("[AUTH] User={} bloqueado por muitas tentativas falhadas - email={} sistemaId={}", usuario.getId(), maskedEmail, sistema.getId());
            usuarioRepository.save(usuario);
            return "Usuário bloqueado devido a muitas tentativas de login falhadas. Para desbloquear, é necessário alterar a senha.";

        }else {
            usuarioRepository.save(usuario);
            return ErrorMessages.Auth.CREDENCIAIS_INVALIDAS;
        }
    }
}
