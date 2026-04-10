package com.api.auth.Application.Service;

import com.api.auth.Application.Exceptions.ValidationException;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Application.Utils.RoleNames;
import com.api.auth.Domain.Entities.RefreshToken;
import com.api.auth.Domain.Entities.UserSession;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.UsuarioSistema;
import com.api.auth.Infra.Repositories.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    public record RefreshRotationResult(Usuario usuario, UserSession session, String refreshToken) {}

    @Value("${JWT_SECRET}")
    private String secret;

    @Value("${JWT_EXPIRATION}") // 15min
    private long expiration;

    @Value("${JWT_REFRESH_EXPIRATION}") // 7 dias
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder encoder;
    private final RefreshTokenService refreshTokenService;

    public JwtService(RefreshTokenRepository refreshTokenRepository, PasswordEncoder encoder, RefreshTokenService refreshTokenService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.encoder = encoder;
        this.refreshTokenService = refreshTokenService;
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UsuarioSistema usuarioSistema) {
        return generateToken(usuarioSistema, null);
    }

    public String generateToken(UsuarioSistema usuarioSistema, UserSession session) {

        Usuario usuario = usuarioSistema.getUsuario();
        List<String> authorities = new ArrayList<>();
        authorities.add(RoleNames.toAuthority(usuarioSistema.getRole().getNome()));
        if (usuario.isGlobalAdmin()) {
            authorities.add(RoleNames.toAuthority(RoleNames.GLOBAL_ADMIN));
        }

        JwtBuilder builder = Jwts.builder()

                .setSubject(usuario.getId().toString()) // id do user
                .claim("nome", usuario.getNome()) // nome do user
                .claim("authorities", authorities)
                .claim("globalAdmin", usuario.isGlobalAdmin())
                .claim("email", usuario.getEmail()) // email do user

                .claim("sistemaId", usuarioSistema.getSistema().getId()) // id do sistema

                .claim("roleId", usuarioSistema.getRole().getId()) // id da role
                .claim("roleNome", usuarioSistema.getRole().getNome()) // nome da role

                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration));

        if (session != null) {
            builder.claim("sessionId", session.getId().toString());
        }

        String token = builder
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();

        log.info("[AUTH] Access token generated - userId={} sistemaId={} roleId={}",
                usuario.getId(), usuarioSistema.getSistema().getId(), usuarioSistema.getRole().getId());
        return token;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("[AUTH] Token validation failed - reason=expired");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[AUTH] Token validation failed - reason=invalid");
            return false;
        }
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }

    ///////// REFRESH TOKEN ///////////

    @Transactional
    public String createRefreshToken(Usuario usuario, UserSession session) {
        log.debug("[AUTH] Issuing refresh token - userId={} sessionId={}", usuario.getId(), session.getId());

        String rawToken = UUID.randomUUID().toString();
        String tokenId = UUID.randomUUID().toString(); // id publico pra busca
        String hashToken = encoder.encode(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenId(tokenId);
        refreshToken.setUsuario(usuario);
        refreshToken.setSession(session);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(hashToken);
        refreshToken.setUsed(false);
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);
        log.info("[AUTH] Refresh token issued - userId={} sessionId={} expiresAt={}",
                usuario.getId(), session.getId(), refreshToken.getExpiryDate());

        return tokenId + ":" + rawToken;
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.warn("[AUTH] Refresh token expired - userId={}", token.getUsuario().getId());
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        log.debug("[AUTH] Refresh token lookup requested");
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshRotationResult rotateRefreshToken(String requestToken) {

        String[] parts = requestToken.split(":", 2);
        if (parts.length != 2)
            throw new ValidationException(ErrorMessages.Recursos.REFRESH_TOKEN_NAO_ENCONTRADO);

        String tokenId = parts[0];
        String rawToken = parts[1];

        RefreshToken currentToken = refreshTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new ValidationException(ErrorMessages.Recursos.REFRESH_TOKEN_NAO_ENCONTRADO));

        UserSession session = currentToken.getSession();
        if (session == null) {
            refreshTokenService.revokeAllByUsuario(currentToken.getUsuario());
            log.error("[AUTH] Legacy refresh token without session detected - userId={} tokenId={}",
                    currentToken.getUsuario().getId(), tokenId);
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }

        if (session.getSistema() == null) {
            refreshTokenService.revokeBySession(session);
            log.error("[AUTH] Legacy refresh token without sistema on session detected - userId={} tokenId={} sessionId={}",
                    currentToken.getUsuario().getId(), tokenId, session.getId());
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }

        if (session.getRevokedAt() != null) {
            throw new ValidationException(ErrorMessages.Auth.SESSAO_EXPIRADA);
        }

        if(!encoder.matches(rawToken, currentToken.getToken())){
            refreshTokenService.revokeBySession(session);
            log.error("[AUTH] Refresh token tampering detected - userId={} tokenId={} sessionId={}",
                    currentToken.getUsuario().getId(), tokenId, session.getId());
            throw new ValidationException(ErrorMessages.Auth.REFRESH_TOKEN_REUSE_DETECTADO);
        }

        Usuario usuario = currentToken.getUsuario();

        if (currentToken.isUsed() || currentToken.isRevoked()) {
            log.error("[AUTH] Refresh token reuse detected - userId={} tokenId={} sessionId={}",
                    usuario.getId(), currentToken.getId(), session.getId());
            refreshTokenService.revokeBySession(session);
            throw new ValidationException(ErrorMessages.Auth.REFRESH_TOKEN_REUSE_DETECTADO);
        }

        verifyExpiration(currentToken);

        currentToken.setUsed(true);
        currentToken.setRevoked(true);
        session.setLastUsedAt(Instant.now());
        refreshTokenRepository.save(currentToken);

        String newRawToken = createRefreshToken(usuario, session);
        log.info("[AUTH] Refresh token rotated - userId={} oldTokenId={} sessionId={}",
                usuario.getId(), currentToken.getId(), session.getId());

        return new RefreshRotationResult(usuario, session, newRawToken);
    }

    //public void deleteByUsuario(Usuario usuario) {
    //    revokeAllByUsuario(usuario);
    //}


    @Transactional
    public int deleteExpiredTokens() {
        int deleted = refreshTokenRepository.deleteAllExpired(Instant.now());
        if (deleted > 0) {
            log.info("[AUTH] Expired refresh tokens cleaned - total={}", deleted);
        } else {
            log.debug("[AUTH] Expired refresh token cleanup - no records found");
        }
        return deleted;
    }
}