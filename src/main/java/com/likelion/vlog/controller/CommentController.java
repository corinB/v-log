package com.likelion.vlog.controller;

import com.likelion.vlog.dto.comments.*;
import com.likelion.vlog.dto.common.ApiResponse;
import com.likelion.vlog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "댓글", description = "댓글 및 답글 CRUD API")
@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록 조회 (답글 포함)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentWithRepliesGetResponse>>> getComments(
            @PathVariable Long postId) {

        List<CommentWithRepliesGetResponse> response = commentService.getComments(postId);
        return ResponseEntity.ok(ApiResponse.success("댓글 목록 조회 성공", response));
    }

    @Operation(summary = "댓글 작성", description = "게시글에 댓글 작성 (인증 필요)")
    @PostMapping
    public ResponseEntity<ApiResponse<CommentPostResponse>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        CommentPostResponse response = commentService.createComment(postId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("댓글 작성 성공", response));
    }

    @Operation(summary = "댓글 수정", description = "댓글 수정 (작성자만 가능)")
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentPutResponse>> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdatePutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        CommentPutResponse response = commentService.updateComment(postId, commentId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("댓글 수정 성공", response));
    }

    @Operation(summary = "댓글 삭제", description = "댓글 삭제 (작성자만 가능)")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<?>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        commentService.deleteComment(postId, commentId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("댓글 삭제 성공"));
    }

    @Operation(summary = "답글 작성", description = "댓글에 답글 작성 (인증 필요)")
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<ReplyPostResponse>> createReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody ReplyCreatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ReplyPostResponse response = commentService.createReply(postId, commentId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("답글 작성 성공", response));
    }

    @Operation(summary = "답글 수정", description = "답글 수정 (작성자만 가능)")
    @PutMapping("/{commentId}/replies/{replyId}")
    public ResponseEntity<ApiResponse<ReplyPutResponse>> updateReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @PathVariable Long replyId,
            @Valid @RequestBody ReplyUpdatePutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ReplyPutResponse response = commentService.updateReply(postId, commentId, replyId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("답글 수정 성공", response));
    }

    @Operation(summary = "답글 삭제", description = "답글 삭제 (작성자만 가능)")
    @DeleteMapping("/{commentId}/replies/{replyId}")
    public ResponseEntity<ApiResponse<?>> deleteReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @PathVariable Long replyId,
            @AuthenticationPrincipal UserDetails userDetails) {

        commentService.deleteReply(postId, commentId, replyId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("답글 삭제 성공"));
    }
}
