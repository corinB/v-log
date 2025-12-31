package com.likelion.vlog.service;

import com.likelion.vlog.exception.BadRequestException;
import com.likelion.vlog.exception.DuplicateException;
import com.likelion.vlog.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("FollowService 커스텀 예외 테스트")
class FollowServiceExceptionTest {

    @Autowired
    private FollowService followService;

    @Test
    @DisplayName("팔로우 - 팔로워(현재 사용자) 없음 시 NotFoundException 발생")
    void follow_FollowerNotFound_ThrowsNotFoundException() {
        // given
        Long userId = 2L;
        String email = "notfound@example.com";

        // when & then
        assertThatThrownBy(() -> followService.follow(userId, email))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("팔로우 - 팔로잉(대상 사용자) 없음 시 NotFoundException 발생")
    void follow_FollowingNotFound_ThrowsNotFoundException() {
        // given
        Long userId = 999999L;
        String email = "test@example.com"; // DB에 있다고 가정

        // when & then
        assertThatThrownBy(() -> followService.follow(userId, email))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("언팔로우 - 팔로워(현재 사용자) 없음 시 NotFoundException 발생")
    void unfollow_FollowerNotFound_ThrowsNotFoundException() {
        // given
        Long userId = 2L;
        String email = "notfound@example.com";

        // when & then
        assertThatThrownBy(() -> followService.unfollow(userId, email))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("언팔로우 - 팔로잉(대상 사용자) 없음 시 NotFoundException 발생")
    void unfollow_FollowingNotFound_ThrowsNotFoundException() {
        // given
        Long userId = 999999L;
        String email = "test@example.com"; // DB에 있다고 가정

        // when & then
        assertThatThrownBy(() -> followService.unfollow(userId, email))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("언팔로우 - 팔로우 관계 없음 시 NotFoundException 발생")
    void unfollow_FollowNotFound_ThrowsNotFoundException() {
        // given
        Long userId = 999999L; // 존재하지 않는 사용자
        String email = "notfound@example.com"; // 존재하지 않는 사용자

        // when & then
        assertThatThrownBy(() -> followService.unfollow(userId, email))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("찾을 수 없습니다");
    }
}
