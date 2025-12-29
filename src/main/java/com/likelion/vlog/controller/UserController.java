package com.likelion.vlog.controller;

import com.likelion.vlog.dto.common.ApiResponse;
import com.likelion.vlog.dto.users.UserGetResponse;
import com.likelion.vlog.dto.users.UserUpdateRequest;
import com.likelion.vlog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tags/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/{user_id}")
    public ResponseEntity<ApiResponse<UserGetResponse>> updateUser(@PathVariable("user_id") Long userId, @RequestBody UserUpdateRequest userUpdateRequest) {

        return ResponseEntity.ok(ApiResponse.success("회원정보 수정 성공", userService.updateUser(userId, userUpdateRequest)));
    }

    @DeleteMapping("/{user_id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable("user_id") Long userId, @RequestBody UserUpdateRequest userUpdateRequest) {

        userService.deleteUser(userId, userUpdateRequest.getPassword());
        return ResponseEntity.ok(ApiResponse.success("회원탈퇴 성공"));
    }

    @GetMapping("/{user_id}")
    public ResponseEntity<ApiResponse<UserGetResponse>> getUser(@PathVariable("user_id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("회원정보 조회 성공", userService.getUser(userId)));
    }
}