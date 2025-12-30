package com.likelion.vlog.exception;

/**
 * 중복 데이터가 존재할 때 발생하는 예외 (409 Conflict)
 */
public class DuplicateException extends RuntimeException {

    public DuplicateException(String message) {
        super(message);
    }

    public static DuplicateException email(String email) {
        return new DuplicateException("이미 존재하는 이메일입니다. email=" + email);
    }

    public static DuplicateException following() {
        return new DuplicateException("이미 팔로우 중입니다.");
    }
}
