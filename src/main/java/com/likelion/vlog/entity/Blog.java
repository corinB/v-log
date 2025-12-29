package com.likelion.vlog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "blogs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Blog extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    /**
     * 블로그 생성 (회원가입 시 자동 생성)
     * - 기본 타이틀: "{닉네임}의 블로그"
     */
    public static Blog create(User user) {
        Blog blog = new Blog();
        blog.user = user;
        blog.title = user.getNickname() + "의 블로그";
        return blog;
    }
}
