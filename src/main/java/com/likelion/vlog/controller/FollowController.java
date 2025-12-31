package com.likelion.vlog.controller;

import com.likelion.vlog.dto.common.ApiResponse;
import com.likelion.vlog.dto.follows.FollowDeleteResponse;
import com.likelion.vlog.dto.follows.FollowPostResponse;
import com.likelion.vlog.dto.follows.FollowerGetResponse;
import com.likelion.vlog.dto.follows.FollowingGetResponse;
import com.likelion.vlog.dto.follows.PageResponse;
import com.likelion.vlog.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "팔로우", description = "팔로우/언팔로우 및 팔로워/팔로잉 조회 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "팔로우", description = "사용자 팔로우 (인증 필요)")
    @PostMapping("/{user_id}/follows")
    public ResponseEntity<ApiResponse<FollowPostResponse>> follow(
            @PathVariable("user_id") Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        FollowPostResponse response = followService.follow(userId, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("팔로우 완료", response));
    }

    @Operation(summary = "언팔로우", description = "사용자 언팔로우 (인증 필요)")
    @DeleteMapping("/{user_id}/follows")
    public ResponseEntity<ApiResponse<FollowDeleteResponse>> unfollow(
            @PathVariable("user_id") Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        FollowDeleteResponse response = followService.unfollow(userId, userDetails.getUsername());
        return ResponseEntity.ok()
                .body(ApiResponse.success("언팔로우 완료", response));
    }

    @Operation(summary = "팔로워 목록 조회", description = "사용자의 팔로워 목록 조회 (페이징)")
    @GetMapping("/{user_id}/followers")
    public ResponseEntity<ApiResponse<PageResponse<FollowerGetResponse>>> getFollowers(@PathVariable("user_id") Long userId, Pageable pageable) {
        Page<FollowerGetResponse> page = followService.getFollowers(userId, pageable);
        PageResponse<FollowerGetResponse> response = new PageResponse<>(
                page.getContent(),
                new PageResponse.PageInfo(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages()
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success("팔로워 목록 조회 성공", response)
        );
    }

    @Operation(summary = "팔로잉 목록 조회", description = "사용자가 팔로우하는 목록 조회 (페이징)")
    @GetMapping("/{user_id}/followings")
    public ResponseEntity<ApiResponse<PageResponse<FollowingGetResponse>>> getFollowings(@PathVariable("user_id") Long userId, Pageable pageable) {
        Page<FollowingGetResponse> page = followService.getFollowings(userId, pageable);
        PageResponse<FollowingGetResponse> response = new PageResponse<>(
                page.getContent(),
                new PageResponse.PageInfo(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages()
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success("팔로잉 목록 조회 성공", response)
        );
    }
}
