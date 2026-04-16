package com.api.auth.API.Config;

import com.api.auth.Application.Service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@Order(2)
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateToken(token)) {
                Claims claims = jwtService.extractClaims(token);

                List<?> authoritiesFromToken = claims.get("authorities", List.class);
                List<GrantedAuthority> authorities = authoritiesFromToken == null
                        ? Collections.emptyList()
                        : authoritiesFromToken.stream()
                            .map(String::valueOf)
                            .map(authority -> (GrantedAuthority) new SimpleGrantedAuthority(authority))
                            .toList();

                // Criar autenticação no contexto do Spring
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, authorities
                );
                auth.setDetails(claims);
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("[AUTH] JWT accepted - userId={} uri={}", claims.getSubject(), request.getRequestURI());
            } else {
                log.warn("[AUTH] JWT rejected - uri={} reason=invalid_token", request.getRequestURI());
            }
        } else {
            log.debug("[AUTH] Request without bearer token - uri={}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}