package com.api.auth.Application.Service;

import com.api.auth.Application.Exceptions.ValidationException;
import com.api.auth.Application.Utils.ErrorMessages;
import com.api.auth.Domain.Entities.RefreshToken;
import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.UsuarioSistema;
import com.api.auth.Infra.Repositories.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    @Value("${JWT_SECRET}")
    private String secret;

    @Value("${JWT_EXPIRATION}") // 15min
    private long expiration;

    @Value("${JWT_REFRESH_EXPIRATION}") // 7 dias
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public JwtService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UsuarioSistema usuarioSistema) {

        Usuario usuario = usuarioSistema.getUsuario();
        String token = Jwts.builder()

                .setSubject(usuario.getId().toString()) // id do user
                .claim("nome", usuario.getNome()) // nome do user
                .claim("email", usuario.getEmail()) // email do user

                .claim("sistemaId", usuarioSistema.getSistema().getId()) // id do sistema

                .claim("roleId", usuarioSistema.getRole().getId()) // id da role
                .claim("roleNome", usuarioSistema.getRole().getNome()) // nome da role

                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
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
    public RefreshToken createRefreshToken(Usuario usuario) {
        log.debug("[AUTH] Issuing refresh token - userId={}", usuario.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUsed(false);
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);
        log.info("[AUTH] Refresh token issued - userId={} expiresAt={}", usuario.getId(), refreshToken.getExpiryDate());
        return refreshToken;
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
    public RefreshToken rotateRefreshToken(String requestToken) {
        RefreshToken currentToken = findByToken(requestToken)
                .orElseThrow(() -> new ValidationException(ErrorMessages.Recursos.REFRESH_TOKEN_NAO_ENCONTRADO));

        Usuario usuario = currentToken.getUsuario();

        if (currentToken.isUsed() || currentToken.isRevoked()) {
            log.error("[AUTH] Refresh token reuse detected - userId={} tokenId={}", usuario.getId(), currentToken.getId());
            revokeAllByUsuario(usuario);
            throw new ValidationException(ErrorMessages.Auth.REFRESH_TOKEN_REUSE_DETECTADO);
        }

        verifyExpiration(currentToken);

        currentToken.setUsed(true);
        currentToken.setRevoked(true);
        refreshTokenRepository.save(currentToken);

        RefreshToken rotated = createRefreshToken(usuario);
        log.info("[AUTH] Refresh token rotated - userId={} oldTokenId={} newTokenId={}",
                usuario.getId(), currentToken.getId(), rotated.getId());
        return rotated;
    }

    public void deleteByUsuario(Usuario usuario) {
        revokeAllByUsuario(usuario);
    }

    @Transactional
    public void revokeAllByUsuario(Usuario usuario) {
        int total = refreshTokenRepository.revokeAllByUsuario(usuario);
        log.info("[AUTH] Refresh tokens revoked - userId={} total={}", usuario.getId(), total);
    }

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