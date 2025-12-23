package com.likelion.vlog.entity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Blog blog;

//    @OneToMany(mappedBy = "user")
//    private List<Comment> comments =  new ArrayList<>();


    private String email;
    private String password;
    private String nickname;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
