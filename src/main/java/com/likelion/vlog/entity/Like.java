package com.likelion.vlog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_user_post",
                        columnNames = {"user_id", "post_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 좋아요 생성 메서드
    public static Like create(User user, Post post) {
        Like like = new Like();
        like.user = user;
        like.post = post;
        return like;
    }
}
