package com.api.auth.Application.Service;

import com.api.auth.Domain.Entities.Usuario;
import com.api.auth.Domain.Entities.UsuarioSistema;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${JWT_SECRET}")
    private String secret;

    @Value("${JWT_EXPIRATION}")
    private long expiration;

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UsuarioSistema usuarioSistema) {

        Usuario usuario = usuarioSistema.getUsuario();
        return Jwts.builder()

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
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }
}