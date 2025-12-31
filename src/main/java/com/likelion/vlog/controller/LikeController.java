package com.likelion.vlog.controller;

import com.likelion.vlog.dto.common.ApiResponse;
import com.likelion.vlog.dto.like.LikeResponse;
import com.likelion.vlog.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "좋아요", description = "게시글 좋아요 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/like")
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "좋아요 정보 조회", description = "게시글의 좋아요 수와 현재 사용자의 좋아요 여부 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<LikeResponse>> getLikes(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = (userDetails != null) ? userDetails.getUsername() : null;

        LikeResponse response = likeService.getLikeInfo(email, postId);

        return ResponseEntity.ok(
                ApiResponse.success("좋아요 조회 성공", response)
        );
    }

    /**
     * Post - 좋아요 추가만
     * Delete - 좋아요 삭제만
     * 한가지 기능 밖에 못해서 프론트엔드에서 처리 필요
     * (이미 좋아요가 있는지 없는지 판단해서 각 메서드 호출)
     *
     * //프론트엔드에서 현재 상태를 보고 판단
     */

    /**
     * EX)
     * async function handleLikeClick(postId, isLiked) {
     *     if (isLiked) {
     *         // 이미 좋아요 눌렀으면 DELETE 호출
     *         await fetch(`/api/posts/${postId}/likes`, { method: 'DELETE' });
     *     } else {
     *         // 안 눌렀으면 POST 호출
     *         await fetch(`/api/posts/${postId}/likes`, { method: 'POST' });
     *     }
     * }
     */

    @Operation(summary = "좋아요 추가", description = "게시글에 좋아요 추가 (인증 필요)")
    @PostMapping
    public ResponseEntity<ApiResponse<LikeResponse>> addLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        LikeResponse response = likeService.addLike(userDetails.getUsername(), postId);
        return ResponseEntity.ok(ApiResponse.success("좋아요 추가 성공", response));
    }

    @Operation(summary = "좋아요 취소", description = "게시글 좋아요 취소 (인증 필요)")
    @DeleteMapping
    public ResponseEntity<ApiResponse<LikeResponse>> removeLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        LikeResponse response = likeService.removeLike(userDetails.getUsername(), postId);
        return ResponseEntity.ok(ApiResponse.success("좋아요 취소 성공", response));
    }
}