package com.likelion.vlog.service;

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
@DisplayName("LikeService 커스텀 예외 테스트")
class LikeServiceExceptionTest {

    @Autowired
    private LikeService likeService;

    @Test
    @DisplayName("좋아요 추가 - 사용자 없음 시 NotFoundException 발생")
    void addLike_UserNotFound_ThrowsNotFoundException() {
        // given
        String email = "notfound@example.com";
        Long postId = 1L;

        // when & then
        assertThatThrownBy(() -> likeService.addLike(email, postId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("좋아요 추가 - 게시글 없음 시 NotFoundException 발생")
    void addLike_PostNotFound_ThrowsNotFoundException() {
        // given
        String email = "notfound@example.com"; // 사용자도 없음
        Long postId = 999999L;

        // when & then
        assertThatThrownBy(() -> likeService.addLike(email, postId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("찾을 수 없습니다");
    }

    @Test
    @DisplayName("좋아요 삭제 - 좋아요 없음 시 NotFoundException 발생")
    void removeLike_LikeNotFound_ThrowsNotFoundException() {
        // given
        String email = "notfound@example.com"; // 사용자도 없음
        Long postId = 1L;

        // when & then
        assertThatThrownBy(() -> likeService.removeLike(email, postId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("찾을 수 없습니다");
    }

    @Test
    @DisplayName("좋아요 정보 조회 - 게시글 없음 시 NotFoundException 발생")
    void getLikeInfo_PostNotFound_ThrowsNotFoundException() {
        // given
        String email = "test@example.com";
        Long postId = 999999L;

        // when & then
        assertThatThrownBy(() -> likeService.getLikeInfo(email, postId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }
}
