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

    public static DuplicateException nickname(String nickname) {
        return new DuplicateException("이미 존재하는 닉네임입니다. nickname=" + nickname);
    }

    public static DuplicateException following() {
        return new DuplicateException("이미 팔로우 중입니다.");
    }

    public static DuplicateException like() {
        return new DuplicateException("이미 좋아요를 누른 게시글입니다.");
    }
}
