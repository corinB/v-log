package com.likelion.vlog.controller;

import com.likelion.vlog.dto.common.ApiResponse;
import com.likelion.vlog.dto.follows.FollowDeleteResponse;
import com.likelion.vlog.dto.follows.FollowPostResponse;
import com.likelion.vlog.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 팔로우/언팔로우 API
 * - Base URL: api/v1/users
 * - 인증 필수
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * 팔로우 (POST /api/v1/users/{user_id}/follow)
     * - 성공 시 201 Created
     * - 자기 자신 팔로우 불가 (400 Bad Request)
     * - 중복 팔로우 불가 (409 Conflict)
     */

    @PostMapping("/{user_id}/follows")
    public ResponseEntity<ApiResponse<FollowPostResponse>> follow(
            @PathVariable("user_id") Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        FollowPostResponse response = followService.follow(userId, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("팔로우 완료", response));
    }

    /**
     * 언팔로우 (DELETE /api/v1/users/{user_id}/follow)
     * - 성공 시 200 OK
     * - 팔로우 상태가 아니라면 404 Not Found
     */
    @DeleteMapping("/{user_id}/follows")
    public ResponseEntity<ApiResponse<FollowDeleteResponse>> unfollow(
            @PathVariable("user_id") Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        FollowDeleteResponse response = followService.unfollow(userId, userDetails.getUsername());
        return ResponseEntity.ok()
                .body(ApiResponse.success("언팔로우 완료", response));
    }
}
