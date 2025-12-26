package com.likelion.vlog.controller;

import com.likelion.vlog.dto.common.ApiResponse;
import com.likelion.vlog.dto.user.UserDto;
import com.likelion.vlog.dto.user.UserUpdateRequestDto;
import com.likelion.vlog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/{user_id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable("user_id") Long userId, @RequestBody UserUpdateRequestDto userUpdateRequestDto) {

        return ResponseEntity.ok(ApiResponse.success("회원정보 수정 성공", userService.updateUser(userId, userUpdateRequestDto)));
    }

    @DeleteMapping("/{user_id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable("user_id") Long userId, @RequestBody UserUpdateRequestDto userUpdateRequestDto) {

        userService.deleteUser(userId, userUpdateRequestDto.getPassword());
        return ResponseEntity.ok(ApiResponse.success("회원탈퇴 성공"));
    }

    @GetMapping("/{user_id}")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable("user_id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success("회원정보 조회 성공", userService.getUser(userId)));
    }
}