package com.likelion.vlog.controller;

import com.likelion.vlog.dto.user.UserResponseDto;
import com.likelion.vlog.dto.user.UserUpdateDto;
import com.likelion.vlog.entity.entity.User;
import com.likelion.vlog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // @PutMapping("/{user_id}")
    // public ResponseEntity<User> updateUser(
    //         @PathVariable("user_id") Long userId,
    //         @RequestBody UserUpdateDto userUpdateDto) {
    //
    //     User updatedUser = userService.updateUser(userId, userUpdateDto);
    //     return ResponseEntity.ok(updatedUser);
    // }
    @PutMapping("/{user_id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable("user_id") Long userId,
            @RequestBody UserUpdateDto userUpdateDto) {

        User updatedUser = userService.updateUser(userId, userUpdateDto);
        return ResponseEntity.ok(new UserResponseDto(updatedUser));
    }


}
