package com.likelion.vlog.entity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "blogs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Blog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

//    @OneToMany(mappedBy = "blog")
//    private List<Post> posts = new ArrayList<>();

    private String title;

    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
