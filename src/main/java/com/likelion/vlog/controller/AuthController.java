package com.likelion.vlog.controller;


import com.likelion.vlog.dto.auth.LoginRequestDto;
import com.likelion.vlog.dto.auth.SignupRequestDto;
import com.likelion.vlog.dto.common.ApiResponse;
import com.likelion.vlog.dto.user.UserDto;
import com.likelion.vlog.service.AuthService;
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
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserDto>> signup(@Valid @RequestBody SignupRequestDto dto) {
        UserDto userDto = authService.signup(dto);
        return ResponseEntity.ok(ApiResponse.success("회원가입 성공", userDto));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(@RequestBody LoginRequestDto req,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        // 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        // SecurityContext 생성 및 인증 정보 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // HttpSession에 저장
        securityContextRepository.saveContext(context, request, response);

        // 사용자 정보 조회 및 반환
        return ResponseEntity.ok(ApiResponse.success(
                "로그인 성공",
                authService.getUserInfo(authentication.getName())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
    }
}
