package com.likelion.vlog.entity;

import com.likelion.vlog.dto.auth.SignupRequest;
import com.likelion.vlog.dto.users.UserUpdateRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends  BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Blog blog;

//    @OneToMany(mappedBy = "user")
//    private List<Comment> comments =  new ArrayList<>();

    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(unique = true, nullable = false)
    private String nickname;


    @PrePersist
    private void prePersist() {
        this.blog = Blog.create(this);
    }


    public void upDateInfo(UserUpdateRequest requestDto, PasswordEncoder passwordEncoder){

        if (requestDto.getNickname() != null) {
            this.nickname = requestDto.getNickname();
        }

        if (requestDto.getPassword() != null) {
            this.password = passwordEncoder.encode(requestDto.getPassword());
        }
    }


    public static User of(SignupRequest signupRequest, PasswordEncoder passwordEncoder){
        User user = new User();
        user.email = signupRequest.getEmail();
        user.password = passwordEncoder.encode(signupRequest.getPassword());
        user.nickname = signupRequest.getNickname();
        return user;
    }

}
