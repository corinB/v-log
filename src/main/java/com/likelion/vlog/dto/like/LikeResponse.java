package com.likelion.vlog.dto.like;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class LikeResponse {
    private Integer likeCount;
    private Boolean checkLike;

    public static LikeResponse from(Integer likeCount, Boolean checkLike) {
        return new LikeResponse(likeCount, checkLike);
    }
}
