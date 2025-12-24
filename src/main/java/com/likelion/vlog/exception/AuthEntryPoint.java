package com.likelion.vlog.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.LocalDateTime;

public class AuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        LocalDateTime currentTimeStamp = LocalDateTime.now();
        String path = request.getRequestURI();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        String jsonRes = String.format("""
                {
                    "message": "인증되지 않았습니다.",
                    "status": "401",
                    "path": "%S",
                    "timestamp": "%s"
                }
                """,path, currentTimeStamp.toString());

        response.getWriter().write(jsonRes);
    }
}
