package com.likelion.vlog.dto.comments;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * POST /api/v1/posts/{postId}/comments/{commentId}/replies 요청 객체
 */
@Getter
@Setter
@NoArgsConstructor
public class ReplyCreatePostRequest {

    @NotBlank(message = "답글 내용은 필수입니다.")
    private String content;
}
