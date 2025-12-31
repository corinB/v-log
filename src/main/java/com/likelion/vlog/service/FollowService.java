package com.likelion.vlog.service;

import com.likelion.vlog.dto.follows.FollowerGetResponse;
import com.likelion.vlog.dto.follows.FollowingGetResponse;
import com.likelion.vlog.dto.follows.FollowDeleteResponse;
import com.likelion.vlog.dto.follows.FollowPostResponse;
import com.likelion.vlog.entity.Follow;
import com.likelion.vlog.entity.User;
import com.likelion.vlog.exception.BadRequestException;
import com.likelion.vlog.exception.DuplicateException;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.repository.FollowRepository;
import com.likelion.vlog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    /**
     * 팔로우
     * - 현재 로그인한 사용자가 특정 사용자를 팔로우
     */
    @Transactional
    public FollowPostResponse follow(Long userId, String email) {
        // 현재 로그인한 사용자 조회
        User follower = userRepository.findByEmail(email)
                .orElseThrow(() -> NotFoundException.user(email));

        // 팔로우 대상 조회
        User following = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));

        // 자기 자신을 팔로우할 수 없음
        if (follower.getId().equals(following.getId())) {
            throw BadRequestException.selfFollow();
        }

        // 이미 팔로우 중인지 확인
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw DuplicateException.following();
        }

        // 팔로우 생성
        Follow follow = Follow.create(follower, following);
        followRepository.save(follow);

        return FollowPostResponse.from(following);
    }

    /**
     * 언팔로우
     * - 현재 로그인한 사용자가 특정 사용자를 언팔로우
     */
    @Transactional
    public FollowDeleteResponse unfollow(Long userId, String email) {
        // 현재 로그인한 사용자 조회
        User follower = userRepository.findByEmail(email)
                .orElseThrow(() -> NotFoundException.user(email));

        // 언팔로우 대상 조회
        User following = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));

        // 팔로우 관계 조회
        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(NotFoundException::follow);

        // 팔로우 삭제
        followRepository.delete(follow);

        return FollowDeleteResponse.from(following);
    }

    /**
     * 팔로잉 조회
     */
    public Page<FollowingGetResponse> getFollowings(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));

        return followRepository.findByFollower(user, pageable)
                .map(follow ->
                        FollowingGetResponse.of(
                                follow.getFollowing(),
                                true
                        )
                );
    }

    /**
     * 팔로워 조회
     */
    public Page<FollowerGetResponse> getFollowers(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));

        return followRepository.findByFollowing(user, pageable)
                .map(follow -> {
                    User follower = follow.getFollower();
                    boolean isFollowing =
                            followRepository.existsByFollowerAndFollowing(user, follower);

                    return FollowerGetResponse.of(follower, isFollowing);
                });
    }
}
