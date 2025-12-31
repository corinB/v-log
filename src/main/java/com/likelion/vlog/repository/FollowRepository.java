package com.likelion.vlog.repository;

import com.likelion.vlog.entity.Follow;
import com.likelion.vlog.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Page<Follow> findByFollower(User follower, Pageable pageable);

    Page<Follow> findByFollowing(User following, Pageable pageable);

    boolean existsByFollowerAndFollowing(User follower, User following);

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
                                
    // User가 팔로우한 모든 관계 삭제
    void deleteAllByFollowerId(Long followerId);

    // User를 팔로우하는 모든 관계 삭제
    void deleteAllByFollowingId(Long followingId);
}
