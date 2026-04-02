package com.api.auth.Application.Service;

import com.api.auth.Application.Exceptions.ValidationException;
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
        log.debug("[AUTH] Rotating refresh token - userId={}", usuario.getId());
        refreshTokenRepository.deleteByUsuario(usuario);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshTokenRepository.save(refreshToken);
        log.info("[AUTH] Refresh token issued - userId={} expiresAt={}", usuario.getId(), refreshToken.getExpiryDate());
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            log.warn("[AUTH] Refresh token expired - userId={}", token.getUsuario().getId());
            throw new ValidationException("Refresh token expirado. Faça login novamente.");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        log.debug("[AUTH] Refresh token lookup requested");
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteByUsuario(Usuario usuario) {
        log.info("[AUTH] Revoking refresh tokens - userId={}", usuario.getId());
        refreshTokenRepository.deleteByUsuario(usuario);
    }
}