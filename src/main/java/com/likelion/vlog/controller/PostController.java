package com.likelion.vlog.controller;

import com.likelion.vlog.dto.posts.PostCreateRequest;
import com.likelion.vlog.dto.posts.PostGetRequest;
import com.likelion.vlog.dto.posts.PostUpdateRequest;
import com.likelion.vlog.dto.posts.response.PageResponse;
import com.likelion.vlog.dto.posts.response.PostListResponse;
import com.likelion.vlog.dto.posts.response.PostResponse;
import com.likelion.vlog.service.PostService;
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
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 목록 조회 (GET /api/v1/posts)
     * - 페이징: ?page=0&size=10
     * - 필터링: ?tag=Spring&blogId=1
     * - 정렬: 기본값 created_at DESC (최신순)
     */
    @GetMapping
    public ResponseEntity<PageResponse<PostListResponse>> getPosts(@ModelAttribute PostGetRequest request) {

        PageResponse<PostListResponse> response = postService.getPosts(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 상세 조회 (GET /api/v1/posts/{postId})
     * - 인증 불필요 (비로그인도 조회 가능)
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
        PostResponse response = postService.getPost(postId);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 작성 (POST /api/v1/posts)
     * - 인증 필요 (SecurityConfig에서 처리)
     * - 성공 시 201 Created
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        PostResponse response = postService.createPost(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 게시글 수정 (PUT /api/v1/posts/{postId})
     * - 인증 필요
     * - 작성자만 수정 가능 (403 Forbidden은 Service에서 처리)
     */
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        PostResponse response = postService.updatePost(postId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 삭제 (DELETE /api/v1/posts/{postId})
     * - 인증 필요
     * - 작성자만 삭제 가능
     * - 성공 시 204 No Content
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {

        postService.deletePost(postId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}