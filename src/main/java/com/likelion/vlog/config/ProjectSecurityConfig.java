package com.likelion.vlog.config;

import com.likelion.vlog.exception.AuthEntryPoint;
import com.likelion.vlog.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
public class ProjectSecurityConfig {


    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws  Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        // TODO: 프론트엔드 연결 시 CORS 설정 필요 (allowedOrigins, allowCredentials 등)
        http.cors(withDefaults());
        http.authorizeHttpRequests(auth -> auth
                        // 인증 X
                        .requestMatchers(HttpMethod.POST, "/auth/signup", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/users/**").permitAll()

                        // 인증 O
                        .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
                        // .requestMatchers(HttpMethod.PUT, "/users/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/users/**").authenticated()

                        .anyRequest().denyAll()
                );

        // SecurityContextRepository 연결 -> Spring Security가 자동으로 세션 관리
        http.securityContext(context -> context
                .securityContextRepository(securityContextRepository())
        );

        http.sessionManagement(session -> session
                .sessionFixation().migrateSession()
        );

        // 인증 실패 에러 처리
        http.exceptionHandling(hbc
                -> hbc.authenticationEntryPoint(new AuthEntryPoint())
        );

        return http.build();
    }

    @Bean
    public HttpSessionSecurityContextRepository securityContextRepository() {
        HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();
        repository.setSpringSecurityContextKey(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        return repository;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthService authService,
            PasswordEncoder passwordEncoder
    ) {
        // 레퍼지토리 기반 인증 제공자 설정
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(authService);
        provider.setPasswordEncoder(passwordEncoder); // 비밀번호 검증 인코더 지정
        return new ProviderManager(provider);
    }

    @Bean //인코더
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
