package com.likelion.vlog.dto.posts;

import com.likelion.vlog.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * GET /api/v1/posts 응답 객체 (목록 조회)
 */
@Getter
@Builder
public class PostListGetResponse {
    private Long postId;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer likeCount;
    private AuthorResponse author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static PostListGetResponse of(Post post) {
        return PostListGetResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(AuthorResponse.from(post.getBlog().getUser()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .build();
    }
}
