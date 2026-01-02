package com.likelion.vlog.dto.posts;

import com.likelion.vlog.dto.comments.CommentWithRepliesGetResponse;
import com.likelion.vlog.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /api/v1/posts/{postId} 응답 객체
 */
@Getter
@Builder
public class PostGetResponse {
    private Long postId;
    private String title;
    private String content;
    private AuthorResponse author;
    private List<String> tags;
    private List<CommentWithRepliesGetResponse> comments;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 정적 팩토리 메서드 (댓글 포함)
     */
    public static PostGetResponse of(Post post, List<String> tags, List<CommentWithRepliesGetResponse> comments) {
        return PostGetResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(AuthorResponse.from(post.getBlog().getUser()))
                .tags(tags)
                .comments(comments)
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * 정적 팩토리 메서드 (댓글 미포함 - 작성/수정 응답용)
     */
    public static PostGetResponse of(Post post, List<String> tags) {
        return of(post, tags, List.of());
    }
}
