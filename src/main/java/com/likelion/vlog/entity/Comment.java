package com.likelion.vlog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    // 댓글 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 어느 게시글의 댓글인지
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 어떤 댓글에 대한 대댓글인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Comment> children = new ArrayList<>();


    private String content;

    // 댓글 생성 메서드
    public static Comment create(User user, Post post, String content) {
        Comment comment = new Comment();
        comment.user = user;
        comment.post = post;
        comment.content = content;
        return comment;
    }

    // 대댓글 생성 메서드
    public static Comment createReply(User user, Post post, Comment parent, String content) {
        Comment reply = new Comment();
        reply.user = user;
        reply.post = post;
        reply.parent = parent;
        reply.content = content;
        return reply;
    }

    // 댓글 수정 메서드
    public void update(String content) {
        this.content = content;
    }
}
