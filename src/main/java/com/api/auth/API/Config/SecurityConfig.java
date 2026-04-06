package com.api.auth.API.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RequestLoggingFilter requestLoggingFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, RequestLoggingFilter requestLoggingFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.requestLoggingFilter = requestLoggingFilter;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2-console/**",
                                "/auth/register",
                                "/auth/login",
                                "/auth/login/verify-code",
                                "/auth/refresh-token",
                                "/auth/esqueci-senha/request",
                                "/auth/esqueci-senha/verify-code",
                                "/auth/esqueci-senha/confirm"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form.disable());

        return http.build();
    }
}
