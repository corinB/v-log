package com.likelion.vlog.dto.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
    @NotBlank
    @Email
    private  String email;

    @NotBlank
    private String password;

    @NotBlank
    private String nickname;
}
