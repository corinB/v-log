package com.likelion.vlog.dto.response;

import com.likelion.vlog.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 목록 조회 응답 DTO
 * - 목록에서는 content 대신 summary(100자 요약) 사용
 * - 좋아요/댓글 수는 Sprint 2에서 구현 예정
 */
@Getter
@Builder
public class PostListResponse {
    private Long postId;
    private String title;
    private String content;
    private AuthorResponse author;
    private LocalDateTime createdAt;

    public static PostListResponse of(Post post) {

        return PostListResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(AuthorResponse.from(post.getBlog().getUser()))
                .createdAt(post.getCreatedAt())
                .build();
    }
}