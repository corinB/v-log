package com.likelion.vlog.dto.follows;

import com.likelion.vlog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * POST /users/{user_id}/follow 응답 객체
 */

@Getter
@AllArgsConstructor
public class FollowPostResponse {


    private Long followingId;
    private String followingNickname;

    public static FollowPostResponse from(User following) {
        return new FollowPostResponse(
                following.getId(),
                following.getNickname());

    }
}
