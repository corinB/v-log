package com.likelion.vlog.exception;

/**
 * 잘못된 요청일 때 발생하는 예외 (400)
 * - 필수 필드 누락
 * - 잘못된 데이터 형식
 * - 비즈니스 규칙 위반
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public static BadRequestException requiredField(String fieldName) {
        return new BadRequestException(fieldName + ": 필수 항목입니다.");
    }

    public static BadRequestException invalidFormat(String fieldName) {
        return new BadRequestException(fieldName + ": 올바른 형식이 아닙니다.");
    }

    public static BadRequestException invalidValue(String fieldName, String reason) {
        return new BadRequestException(fieldName + ": " + reason);
    }

    public static BadRequestException selfFollow() {
        return new BadRequestException("자기 자신을 팔로우할 수 없습니다.");
    }
}