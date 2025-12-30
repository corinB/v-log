package com.likelion.vlog.dto.follows;

import com.likelion.vlog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DELETE /users/{user_id}/follow 응답 객체
 */
@Getter
@AllArgsConstructor
public class FollowDeleteResponse {
    private Long unfollowedId;
    private String unfollowedNickname;

    public static FollowDeleteResponse from(User unfollowed) {
        return new FollowDeleteResponse(
                unfollowed.getId(),
                unfollowed.getNickname());
    }
}
