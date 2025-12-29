package com.likelion.vlog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "follows",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_follower_following",
                        columnNames = {"follower_id", "following_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follow_id")
    private Long id;

    // 팔로우 하는사람
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    //팔로우 대상
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    // 팔로우 생성 메서드
    public static Follow create(User follower, User following) {
        Follow follow = new Follow();
        follow.follower = follower;
        follow.following = following;
        return follow;
    }
}
