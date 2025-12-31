package com.likelion.vlog.exception;

/**
 * 인증되지 않은 사용자가 접근할 때 발생하는 예외 (401)
 * - 비회원의 인증 필요 기능 접근
 * - 로그인 필요
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public static UnauthorizedException loginRequired() {
        return new UnauthorizedException("로그인 후 이용하세요.");
    }

    public static UnauthorizedException loginRequiredForFeature(String featureName) {
        return new UnauthorizedException("로그인 후 " + featureName + "을(를) 이용하세요.");
    }
}