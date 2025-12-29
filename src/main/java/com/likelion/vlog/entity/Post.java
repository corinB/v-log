package com.likelion.vlog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String title;

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "view_count")
    private int viewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id")
    private Blog blog;

    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private List<TagMap> tagMapList = new ArrayList<>();

    // 게시글 생성 메서드
    public static Post create(String title, String content, Blog blog) {
        Post post = new Post();
        post.title = title;
        post.content = content;
        post.blog = blog;
        post.viewCount = 0;
        return post;
    }

    // 게시글 수정 메서드
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
