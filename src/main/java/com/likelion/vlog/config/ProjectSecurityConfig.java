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

import org.springframework.web.cors.CorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
public class ProjectSecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable());

        // CORS 설정 적용 (CorsConfig에서 주입)
        http.cors(cors -> cors.configurationSource(corsConfigurationSource));

        http.authorizeHttpRequests(auth -> auth
                        // 인증 X
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/users/**", //사용자 조회
                                "/api/v1/tags/**", //태그 조회
                                "/api/v1/posts/**" //게시글 조회
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/signup", //회원가입
                                "/api/v1/auth/login" //로그인
                        ).permitAll()

                .requestMatchers(HttpMethod.GET,
                        "/api/v1/users/*",      // 사용자 프로필 조회
                        "/api/v1/posts",        // 전체 게시글 조회
                        "/api/v1/posts/*",      // 게시글 상세 조회
                        "/api/v1/tags/*",        // 태그 이름으로 조회
                        "/api/v1/posts/*/like",   //좋아요 조회
                        "/api/v1/users/*/followers", // 팔로워 조회
                        "/api/v1/users/*/followings" // 팔로잉 조회
                ).permitAll()

                // Swagger UI
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**"
                ).permitAll()

                // -------------------------------------------------
                // 2) 인증 O (Authenticated)
                // -------------------------------------------------
                .requestMatchers(HttpMethod.POST,
                        "/api/v1/auth/logout",

                        "/api/v1/posts",                         // 게시글 작성
                        "/api/v1/posts/*/comments",              // 댓글 작성
                        "/api/v1/posts/*/comments/*/replies",    // 답글 생성
                        "/api/v1/posts/*/like",                  // 좋아요

                        "/api/v1/users/*/follows"                 // 팔로우
                ).authenticated()

                .requestMatchers(HttpMethod.PUT,
                        "/api/v1/users/*",                       // 사용자 정보 수정 (본인 검증은 별도)
                        "/api/v1/posts/*",                       // 게시글 수정 (작성자 검증은 별도)
                        "/api/v1/posts/*/comments/*",            // 댓글 수정 (작성자 검증은 별도)
                        "/api/v1/posts/*/comments/*/replies/*"   // 답글 수정 (작성자 검증은 별도)
                ).authenticated()

                .requestMatchers(HttpMethod.DELETE,
                        "/api/v1/users/*",                       // 회원 탈퇴 (본인 검증은 별도)
                        "/api/v1/posts/*",                       // 게시글 삭제 (작성자 검증은 별도)
                        "/api/v1/posts/*/comments/*",            // 댓글 삭제 (작성자 검증은 별도)
                        "/api/v1/posts/*/comments/*/replies/*",  // 답글 삭제 (작성자 검증은 별도)

                        "/api/v1/posts/*/like",                  // 좋아요 취소
                        "/api/v1/users/*/follows"                 // 언팔로우
                ).authenticated()

                // -------------------------------------------------
                // 3) 그 외 전부 차단
                // -------------------------------------------------
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
