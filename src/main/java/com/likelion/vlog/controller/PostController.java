package com.likelion.vlog.controller;

import com.likelion.vlog.dto.common.ApiResponse;
import com.likelion.vlog.dto.posts.*;
import com.likelion.vlog.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글 API 컨트롤러
 * - Base URL: /api/v1/posts
 * - 인증이 필요한 API는 @AuthenticationPrincipal로 사용자 정보 획득
 */
@Tag(name = "게시글", description = "게시글 CRUD API")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회", description = "페이징, 태그/블로그 필터링 지원")
    @GetMapping
    public ResponseEntity<PageResponse<PostListGetResponse>> getPosts(@ModelAttribute PostGetRequest request) {

        PageResponse<PostListGetResponse> response = postService.getPosts(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 상세 조회", description = "댓글 포함 게시글 상세 정보 조회")
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostGetResponse>> getPost(@PathVariable Long postId) {
        PostGetResponse response = postService.getPost(postId);
        return ResponseEntity.ok(ApiResponse.success("게시글 조회 성공", response));
    }

    @Operation(summary = "게시글 작성", description = "새 게시글 작성 (인증 필요)")
    @PostMapping
    public ResponseEntity<ApiResponse<PostGetResponse>> createPost(
            @Valid @RequestBody PostCreatePostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        PostGetResponse response = postService.createPost(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("게시글 작성 성공", response));
    }

    @Operation(summary = "게시글 수정", description = "게시글 수정 (작성자만 가능)")
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostGetResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdatePutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        PostGetResponse response = postService.updatePost(postId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("게시글 수정 성공", response));
    }

    @Operation(summary = "게시글 삭제", description = "게시글 삭제 (작성자만 가능)")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        postService.deletePost(postId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}