package com.likelion.vlog.exception;

/**
 * 인증 정보가 유효하지 않을 때 발생하는 예외 (401)
 * - 이메일/비밀번호 불일치
 * - 비밀번호 확인 실패
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public static InvalidCredentialsException login() {
        return new InvalidCredentialsException("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    public static InvalidCredentialsException password() {
        return new InvalidCredentialsException("비밀번호가 일치하지 않습니다.");
    }
}