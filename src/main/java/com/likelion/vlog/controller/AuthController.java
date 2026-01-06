package com.likelion.vlog.controller;


import com.likelion.vlog.dto.auth.LoginRequest;
import com.likelion.vlog.dto.auth.SignupRequest;
import com.likelion.vlog.dto.common.ApiResponse;
import com.likelion.vlog.dto.users.UserGetResponse;
import com.likelion.vlog.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "회원가입, 로그인, 로그아웃 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 회원가입")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserGetResponse>> signup(@Valid @RequestBody SignupRequest dto) {
        UserGetResponse userGetresponse = authService.signup(dto);
        return ResponseEntity.ok(ApiResponse.success("회원가입 성공", userGetresponse));
    }

    @Operation(summary = "로그인", description = "이메일, 비밀번호로 로그인 (세션 기반)")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserGetResponse>> login(@RequestBody LoginRequest req,
                                                              HttpServletRequest request,
                                                              HttpServletResponse response) {
        // 1. 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        //2.세션 없으면 생성
        request.getSession(true);

        // 3. SessionAuthenticationStrategy 호출
        // (동시 세션 제어, 기존 세션 만료, SessionRegistry 등록)
        sessionAuthenticationStrategy.onAuthentication(authentication, request, response);

        // 4. SecurityContext 생성 및 인증 정보 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 5. HttpSession에 저장
        securityContextRepository.saveContext(context, request, response);

        // 사용자 정보 조회 및 반환
        return ResponseEntity.ok(ApiResponse.success(
                "로그인 성공",
                authService.getUserInfo(authentication.getName())));
    }

    @Operation(summary = "로그아웃", description = "세션 무효화 및 로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            // session.invalidate() 호출 시 HttpSessionEventPublisher가
            // 자동으로 SessionRegistry에서 세션 제거
            session.invalidate();
        }

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
    }
}
