package com.likelion.vlog.service;

import com.likelion.vlog.dto.users.UserGetResponse;
import com.likelion.vlog.dto.users.UserUpdateRequest;

import com.likelion.vlog.entity.User;
import com.likelion.vlog.exception.ForbiddenException;
import com.likelion.vlog.exception.InvalidCredentialsException;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final FollowRepository followRepository;
    private final TagMapRepository tagMapRepository;
    private final PostRepository postRepository;

    public UserGetResponse getUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));
        return UserGetResponse.of(user);
    }

    @Transactional
    public UserGetResponse updateUser(Long userId, UserUpdateRequest userUpdateRequest, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));

        // 권한 검증: 본인만 수정 가능
        if (!user.getEmail().equals(email)) {
            throw ForbiddenException.userUpdate();
        }

        user.upDateInfo(userUpdateRequest, passwordEncoder);

        userRepository.save(user);
        return UserGetResponse.of(user);
    }


    /**
     * 회원 탈퇴
     * - 권한 검증 및 비밀번호 확인 후 User 및 모든 연관 데이터 삭제
     * - 삭제 순서:
     *   1. User가 직접 작성/생성한 것들 (댓글, 좋아요, 팔로우)
     *   2. User의 Blog에 속한 Post들의 연관 데이터 (댓글, 좋아요, 태그)
     *   3. User의 Blog에 속한 Post들
     *   4. Blog (cascade로 자동 삭제)
     *   5. User
     */
    @Transactional
    public void deleteUser(Long userId, String password, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));

        // 권한 검증: 본인만 탈퇴 가능
        if (!user.getEmail().equals(email)) {
            throw ForbiddenException.userDelete();
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw InvalidCredentialsException.password();
        }

        deleteRelationship(userId);

        // 4-5단계: Blog은 cascade=ALL이므로 User 삭제 시 자동 삭제됨
        userRepository.delete(user);
    }


    private void deleteRelationship(Long userId) {
        // 1단계: User가 직접 작성/생성한 것들 삭제
        commentRepository.deleteAllByUserId(userId);           // 내가 쓴 댓글
        likeRepository.deleteAllByUserId(userId);              // 내가 누른 좋아요
        followRepository.deleteAllByFollowerId(userId);        // 내가 팔로우한 관계
        followRepository.deleteAllByFollowingId(userId);       // 나를 팔로우한 관계
        // 2단계: User의 Blog에 속한 Post들의 연관 데이터 삭제
        commentRepository.deleteAllByPostBlogUserId(userId);   // 내 게시글의 댓글들
        likeRepository.deleteAllByPostBlogUserId(userId);      // 내 게시글의 좋아요들
        tagMapRepository.deleteAllByPostBlogUserId(userId);    // 내 게시글의 태그들
        // 3단계: User의 Blog에 속한 Post들 삭제
        postRepository.deleteAllByBlogUserId(userId);
    }

}
