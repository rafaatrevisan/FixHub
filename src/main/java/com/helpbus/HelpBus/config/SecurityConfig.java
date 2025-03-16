package com.helpbus.HelpBus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())  // Disables CSRF protection
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // Apply CORS configuration
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/token/**").permitAll()  // Permite acesso sem autenticação para /token/**
                        .anyRequest().authenticated()  // Requer autenticação para qualquer outro endpoint
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Stateless session
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://example.com"));  // Set allowed origins
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));  // Set allowed methods
        configuration.setAllowedHeaders(List.of("*"));  // Allow all headers
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // Apply CORS configuration to all endpoints
        return source;
    }
}