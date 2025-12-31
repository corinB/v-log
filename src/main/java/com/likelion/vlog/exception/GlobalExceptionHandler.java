package com.likelion.vlog.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 전역 예외 핸들러
 * - 커스텀 예외를 HTTP 상태 코드로 변환
 * - 일관된 에러 응답 형식 제공
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404 Not Found - 리소스를 찾을 수 없음
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundException e) {
        log.warn("NotFoundException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    /**
     * 403 Forbidden - 권한 없음
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbiddenException(ForbiddenException e) {
        log.warn("ForbiddenException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorResponse(HttpStatus.FORBIDDEN, e.getMessage()));
    }

    /**
     * 409 Conflict - 중복 데이터
     */
    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateException(DuplicateException e) {
        log.warn("DuplicateException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse(HttpStatus.CONFLICT, e.getMessage()));
    }

    /**
     * 401 Unauthorized - 인증되지 않은 접근 (로그인 필요)
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("UnauthorizedException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse(HttpStatus.UNAUTHORIZED, e.getMessage()));
    }

    /**
     * 401 Unauthorized - 인증 정보 불일치 (비밀번호 오류 등)
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentialsException(InvalidCredentialsException e) {
        log.warn("InvalidCredentialsException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse(HttpStatus.UNAUTHORIZED, e.getMessage()));
    }

    /**
     * 401 Unauthorized - Spring Security 인증 실패
     */
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            org.springframework.security.authentication.BadCredentialsException e) {
        log.warn("BadCredentialsException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."));
    }

    /**
     * 400 Bad Request - 커스텀 잘못된 요청
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException e) {
        log.warn("BadRequestException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    /**
     * 400 Bad Request - IllegalArgumentException (레거시 지원)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    /**
     * 400 Bad Request - Validation 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("유효성 검사 실패");

        log.warn("ValidationException: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse(HttpStatus.BAD_REQUEST, message));
    }

    /**
     * 500 Internal Server Error - 예상치 못한 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Unexpected error: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."));
    }

    private Map<String, Object> errorResponse(HttpStatus status, String message) {
        return Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "timestamp", LocalDateTime.now().toString()
        );
    }
}