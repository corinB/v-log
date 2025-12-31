package com.likelion.vlog.service;

import com.likelion.vlog.dto.auth.SignupRequest;
import com.likelion.vlog.exception.DuplicateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("AuthService 커스텀 예외 테스트")
class AuthServiceExceptionTest {

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("loadUserByUsername - 사용자 없음 시 UsernameNotFoundException 발생")
    void loadUserByUsername_UserNotFound_ThrowsUsernameNotFoundException() {
        // given
        String email = "notfound@example.com";

        // when & then
        assertThatThrownBy(() -> authService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("없는 이메일");
    }

    @Test
    @DisplayName("getUserInfo - 사용자 없음 시 UsernameNotFoundException 발생")
    void getUserInfo_UserNotFound_ThrowsUsernameNotFoundException() {
        // given
        String email = "notfound@example.com";

        // when & then
        assertThatThrownBy(() -> authService.getUserInfo(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("없는 이메일");
    }
}