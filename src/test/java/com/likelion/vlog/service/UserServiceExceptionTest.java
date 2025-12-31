package com.likelion.vlog.service;

import com.likelion.vlog.dto.users.UserUpdateRequest;
import com.likelion.vlog.exception.InvalidCredentialsException;
import com.likelion.vlog.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("UserService 커스텀 예외 테스트")
class UserServiceExceptionTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("사용자 조회 - 사용자 없음 시 NotFoundException 발생")
    void getUser_UserNotFound_ThrowsNotFoundException() {
        // given
        Long userId = 999999L;

        // when & then
        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자 수정 - 사용자 없음 시 NotFoundException 발생")
    void updateUser_UserNotFound_ThrowsNotFoundException() {
        // given
        Long userId = 999999L;
        UserUpdateRequest request = new UserUpdateRequest("newNickname", null);

        // when & then
        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자 탈퇴 - 사용자 없음 시 NotFoundException 발생")
    void deleteUser_UserNotFound_ThrowsNotFoundException() {
        // given
        Long userId = 999999L;
        String password = "password123";

        // when & then
        assertThatThrownBy(() -> userService.deleteUser(userId, password))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자 탈퇴 - 비밀번호 불일치 시 InvalidCredentialsException 발생")
    void deleteUser_InvalidPassword_ThrowsInvalidCredentialsException() {
        // given
        Long userId = 1L; // DB에 있다고 가정
        String wrongPassword = "wrongPassword";

        // when & then
        assertThatThrownBy(() -> userService.deleteUser(userId, wrongPassword))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }
}