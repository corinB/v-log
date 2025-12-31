package com.likelion.vlog.dto.posts;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * POST /api/v1/posts 요청 객체
 */
@Getter
@Setter
@NoArgsConstructor
public class PostCreatePostRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private List<String> tags;
}
