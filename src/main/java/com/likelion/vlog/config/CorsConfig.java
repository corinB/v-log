package com.likelion.vlog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS(Cross-Origin Resource Sharing) 설정
 * 프론트엔드와 백엔드 간 안전한 크로스 오리진 통신을 위한 설정
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.exposed-headers}")
    private String exposedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${cors.max-age}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origins 설정
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));

        // Methods 설정
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));

        // Headers 설정
        if ("*".equals(allowedHeaders)) {
            configuration.addAllowedHeader("*");
        } else {
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }

        // Exposed Headers 설정
        if ("*".equals(exposedHeaders)) {
            configuration.addExposedHeader("*");
        } else {
            configuration.setExposedHeaders(Arrays.asList(exposedHeaders.split(",")));
        }

        // Credentials 설정 (세션 쿠키 전송 필수)
        configuration.setAllowCredentials(allowCredentials);

        // Preflight 캐시 시간
        configuration.setMaxAge(maxAge);

        // 모든 경로에 CORS 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}