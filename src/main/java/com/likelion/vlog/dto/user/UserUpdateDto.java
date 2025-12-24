package com.likelion.vlog.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserUpdateDto {
    private String nickname;
    private String password;
}
