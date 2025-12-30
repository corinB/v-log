package com.likelion.vlog.service;

import com.likelion.vlog.dto.comments.*;
import com.likelion.vlog.entity.Comment;
import com.likelion.vlog.entity.Post;
import com.likelion.vlog.entity.User;
import com.likelion.vlog.exception.ForbiddenException;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.repository.CommentRepository;
import com.likelion.vlog.repository.PostRepository;
import com.likelion.vlog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 댓글/대댓글 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 게시글의 댓글 목록 조회 (대댓글 포함)
     */
    public List<CommentWithRepliesResponse> getComments(Long postId) {
        Post post = findPostById(postId);

        List<Comment> comments = commentRepository.findAllByPostWithChildren(post);

        return comments.stream()
                .map(CommentWithRepliesResponse::from)
                .toList();
    }

    /**
     * 댓글 작성
     */
    @Transactional
    public CommentPostResponse createComment(Long postId, CommentCreatePostRequest request, String email) {
        Post post = findPostById(postId);
        User user = findUserByEmail(email);

        Comment comment = Comment.of(user, post, request.getContent());
        Comment savedComment = commentRepository.save(comment);

        return CommentPostResponse.from(savedComment);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentPutResponse updateComment(Long postId, Long commentId, CommentUpdatePutRequest request, String email) {
        Post post = findPostById(postId);
        Comment comment = findCommentById(commentId);

        validateCommentBelongsToPost(comment, post);
        validateCommentIsNotReply(comment);
        validateOwnership(comment, email);

        comment.update(request.getContent());

        return CommentPutResponse.from(comment);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long postId, Long commentId, String email) {
        Post post = findPostById(postId);
        Comment comment = findCommentById(commentId);

        validateCommentBelongsToPost(comment, post);
        validateCommentIsNotReply(comment);
        validateOwnership(comment, email);

        commentRepository.delete(comment);
    }

    /**
     * 답글 작성
     */
    @Transactional
    public ReplyPostResponse createReply(Long postId, Long commentId, ReplyCreatePostRequest request, String email) {
        Post post = findPostById(postId);
        Comment parentComment = findCommentById(commentId);

        validateCommentBelongsToPost(parentComment, post);
        validateCommentIsNotReply(parentComment);

        User user = findUserByEmail(email);

        Comment reply = Comment.ofReply(user, post, parentComment, request.getContent());
        Comment savedReply = commentRepository.save(reply);

        return ReplyPostResponse.from(savedReply);
    }

    /**
     * 답글 수정
     */
    @Transactional
    public ReplyPutResponse updateReply(Long postId, Long commentId, Long replyId, ReplyUpdatePutRequest request, String email) {
        Post post = findPostById(postId);
        Comment parentComment = findCommentById(commentId);
        Comment reply = findCommentById(replyId);

        validateCommentBelongsToPost(parentComment, post);
        validateReplyBelongsToComment(reply, parentComment);
        validateOwnership(reply, email);

        reply.update(request.getContent());

        return ReplyPutResponse.from(reply);
    }

    /**
     * 답글 삭제
     */
    @Transactional
    public void deleteReply(Long postId, Long commentId, Long replyId, String email) {
        Post post = findPostById(postId);
        Comment parentComment = findCommentById(commentId);
        Comment reply = findCommentById(replyId);

        validateCommentBelongsToPost(parentComment, post);
        validateReplyBelongsToComment(reply, parentComment);
        validateOwnership(reply, email);

        commentRepository.delete(reply);
    }

    // === Helper Methods ===

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> NotFoundException.post(postId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> NotFoundException.user(email));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다. id=" + commentId));
    }

    private void validateCommentBelongsToPost(Comment comment, Post post) {
        if (!comment.getPost().getId().equals(post.getId())) {
            throw new NotFoundException("해당 게시글의 댓글이 아닙니다.");
        }
    }

    private void validateCommentIsNotReply(Comment comment) {
        if (comment.getParent() != null) {
            throw new NotFoundException("댓글을 찾을 수 없습니다. id=" + comment.getId());
        }
    }

    private void validateReplyBelongsToComment(Comment reply, Comment parentComment) {
        if (reply.getParent() == null || !reply.getParent().getId().equals(parentComment.getId())) {
            throw new NotFoundException("해당 댓글의 답글이 아닙니다.");
        }
    }

    private void validateOwnership(Comment comment, String email) {
        if (!comment.getUser().getEmail().equals(email)) {
            throw new ForbiddenException("댓글 수정/삭제 권한이 없습니다.");
        }
    }
}
