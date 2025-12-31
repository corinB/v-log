package com.likelion.vlog.exception;

/**
 * 권한이 없을 때 발생하는 예외 (403)
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public static ForbiddenException postUpdate() {
        return new ForbiddenException("게시글 수정 권한이 없습니다.");
    }

    public static ForbiddenException postDelete() {
        return new ForbiddenException("게시글 삭제 권한이 없습니다.");
    }

    public static ForbiddenException userUpdate() {
        return new ForbiddenException("사용자 정보 수정 권한이 없습니다.");
    }

    public static ForbiddenException userDelete() {
        return new ForbiddenException("회원 탈퇴 권한이 없습니다.");
    }

    public static ForbiddenException commentUpdate() {
        return new ForbiddenException("댓글 수정 권한이 없습니다.");
    }

    public static ForbiddenException commentDelete() {
        return new ForbiddenException("댓글 삭제 권한이 없습니다.");
    }

}