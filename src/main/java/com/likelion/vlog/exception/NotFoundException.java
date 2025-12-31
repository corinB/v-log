package com.likelion.vlog.exception;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외 (404)
 * NotFoundExceptionType
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException post(Long postId) {
        return new NotFoundException("게시글을 찾을 수 없습니다. id=" + postId);
    }

    public static NotFoundException user(Long userId) {
        return new NotFoundException("사용자를 찾을 수 없습니다. id=" + userId);
    }

    public static NotFoundException user(String email) {return new NotFoundException("사용자를 찾을 수 없습니다. email=" + email);}
    public static NotFoundException userNickname(String nickname){
        return new NotFoundException("사용자를 찾을 수 없습니다. nickname=" + nickname);
    }

    public static NotFoundException blog(Long userId) {
        return new NotFoundException("블로그를 찾을 수 없습니다. userId=" + userId);
    }

    public static NotFoundException follow() {
        return new NotFoundException("팔로우를 찾을 수 없습니다.");
    }

    public static NotFoundException like() {
        return new NotFoundException("좋아요를 찾을 수 없습니다.");
    }

    public static NotFoundException comment(Long commentId) {return new NotFoundException("댓글을 찾을 수 없습니다. id=" + commentId);}

    public static NotFoundException comment(Long postId, Long commentId) {
        return new NotFoundException("해당 게시글의 댓글이 아닙니다. postId=" + postId + ", commentId=" + commentId);
    }

    public static NotFoundException reply(Long commentId, Long replyId) {
        return new NotFoundException("해당 댓글의 답글이 아닙니다. , commentId=" + commentId + ", replyId=" + replyId);
    }

}
