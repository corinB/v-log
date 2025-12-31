package com.likelion.vlog.controller;

import com.likelion.vlog.dto.common.ApiResponse;
import com.likelion.vlog.dto.users.UserGetResponse;
import com.likelion.vlog.dto.users.UserUpdateRequest;
import com.likelion.vlog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자", description = "사용자 정보 조회/수정/탈퇴 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "회원정보 수정", description = "사용자 정보 수정 (인증 필요)")
    @PutMapping("/{user_id}")
    public ResponseEntity<ApiResponse<UserGetResponse>> updateUser(
            @PathVariable("user_id") Long userId,
            @RequestBody UserUpdateRequest userUpdateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        return ResponseEntity.ok(ApiResponse.success("회원정보 수정 성공", userService.updateUser(userId, userUpdateRequest, email)));
    }

    @Operation(summary = "회원탈퇴", description = "회원 탈퇴 (비밀번호 확인 필요)")
    @DeleteMapping("/{user_id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable("user_id") Long userId,
            @RequestBody UserUpdateRequest userUpdateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        userService.deleteUser(userId, userUpdateRequest.getPassword(), email);
        return ResponseEntity.ok(ApiResponse.success("회원탈퇴 성공"));
    }

    @Operation(summary = "회원정보 조회", description = "사용자 정보 조회")
    @GetMapping("/{user_id}")
    public ResponseEntity<ApiResponse<UserGetResponse>> getUser(@PathVariable("user_id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("회원정보 조회 성공", userService.getUser(userId)));
    }
}