package com.campus.secondhand.common.security.config;

import com.campus.secondhand.common.security.jwt.JwtAuthenticationFilter;
import com.campus.secondhand.common.security.jwt.JwtTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public JwtTokenService jwtTokenService() {
        return new JwtTokenService(
            "campus-secondhand-secret-key-2026",
            86400000L
        );
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        return new JwtAuthenticationFilter(jwtTokenService);
    }
}
